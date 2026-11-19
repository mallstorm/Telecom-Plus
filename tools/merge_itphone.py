#!/usr/bin/env python3
"""Встраивает интерфейс Telecom+ в официальный IT Phone (ключ-в-ключ, без публикации оригинала).

usage: merge_itphone.py <splits_dir | base.apk> <Telecom-Plus.apk> <out.apk> [--tools DIR] [--res DIR] [--ks FILE]
"""
import os, re, shutil, subprocess, sys, zipfile, glob, tempfile

def run(*a, **k):
    print('+', ' '.join(a), flush=True)
    subprocess.run(list(a), check=True, **k)

def main():
    args = sys.argv[1:]
    opt = {'--tools': '/tmp/tools', '--res': os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'app/src/main/res'), '--ks': os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', 'app/release.jks')}
    pos = []
    i = 0
    while i < len(args):
        if args[i] in opt:
            opt[args[i]] = args[i + 1]; i += 2
        else:
            pos.append(args[i]); i += 1
    src, tp_apk, out = pos
    tools = opt['--tools']
    work = tempfile.mkdtemp(prefix='itmerge_')
    apks = [src] if os.path.isfile(src) else sorted(glob.glob(os.path.join(src, '*.apk')))
    if len(apks) > 1:
        merged = os.path.join(work, 'merged.apk')
        run('java', '-jar', f'{tools}/APKEditor.jar', 'm', '-i', src, '-o', merged)
    else:
        merged = apks[0]
    proj = os.path.join(work, 'proj')
    run('java', '-jar', f'{tools}/apktool.jar', 'd', '-s', '-f', '-o', proj, merged)

    mp = os.path.join(proj, 'AndroidManifest.xml')
    m = open(mp, encoding='utf-8').read()
    m = re.sub(r'\s+android:requiredSplitTypes="[^"]*"', '', m)
    m = re.sub(r'\s+android:splitTypes="[^"]*"', '', m)
    m = re.sub(r'\s*<meta-data android:name="com\.android\.vending\.splits[^"]*"[^>]*/>', '', m)
    m = re.sub(r'\s*<meta-data android:name="com\.android\.vending\.derived\.apk\.id"[^>]*/>', '', m)
    # убираем LAUNCHER у родной MainActivity
    a = m.index('android:name="smile.MainActivity"')
    s0 = m.rfind('<activity', 0, a)
    e0 = m.index('</activity>', a)
    block = m[s0:e0]
    block2 = re.sub(r'\s*<intent-filter>\s*<action android:name="android\.intent\.action\.MAIN"/>\s*<category android:name="android\.intent\.category\.LAUNCHER"/>\s*</intent-filter>', '', block, count=1)
    assert block != block2, 'LAUNCHER filter not found'
    m = m[:s0] + block2 + m[e0:]
    ours = '''
        <activity android:name="ua.telecomplus.app.MainActivity" android:label="Telecom+" android:icon="@drawable/tp_ic_telecom_plus" android:roundIcon="@drawable/tp_ic_telecom_plus" android:theme="@style/TpTheme" android:launchMode="singleTask" android:exported="true" android:windowSoftInputMode="adjustResize" android:configChanges="orientation|screenSize|keyboardHidden">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        <service android:name="ua.telecomplus.app.SessionKeepAliveJob" android:exported="false" android:permission="android.permission.BIND_JOB_SERVICE"/>
    '''
    m = m.replace('</application>', ours + '</application>', 1)
    open(mp, 'w', encoding='utf-8').write(m)

    res = os.path.join(proj, 'res')
    os.makedirs(os.path.join(res, 'drawable'), exist_ok=True)
    for f in glob.glob(os.path.join(opt['--res'], 'drawable', 'tp_*.xml')):
        shutil.copy(f, os.path.join(res, 'drawable'))
    open(os.path.join(res, 'values', 'tp_values.xml'), 'w', encoding='utf-8').write('''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="TpTheme" parent="@android:style/Theme.Material.Light.NoActionBar">
        <item name="android:windowActionModeOverlay">true</item>
        <item name="android:fontFamily">sans-serif</item>
        <item name="android:colorAccent">#0F5CC0</item>
        <item name="android:statusBarColor">#0A2A66</item>
        <item name="android:navigationBarColor">#ffffff</item>
        <item name="android:windowLightStatusBar">false</item>
    </style>
</resources>
''')
    built = os.path.join(work, 'built.apk')
    run('java', '-jar', f'{tools}/apktool.jar', 'b', '-f', '-o', built, proj)

    # добавляем наш dex
    final_unsigned = os.path.join(work, 'final_unsigned.apk')
    with zipfile.ZipFile(built) as zi, zipfile.ZipFile(tp_apk) as zt:
        dexes = sorted(n for n in zi.namelist() if re.fullmatch(r'classes\d*\.dex', n))
        nxt = len(dexes) + 1
        tpdex = zt.read('classes.dex')
        with zipfile.ZipFile(final_unsigned, 'w') as zo:
            for it in zi.infolist():
                if it.filename.startswith('META-INF/') and re.search(r'\.(RSA|SF|DSA|EC)$|MANIFEST\.MF', it.filename):
                    continue
                data = zi.read(it.filename)
                comp = zipfile.ZIP_STORED if (it.filename.endswith(('.dex', '.arsc', '.so', '.png', '.ogg', '.wav')) or it.compress_type == 0) else zipfile.ZIP_DEFLATED
                zo.writestr(it.filename, data, compress_type=comp)
            zo.writestr(f'classes{nxt}.dex', tpdex, compress_type=zipfile.ZIP_STORED)
    outdir = os.path.join(work, 'signed')
    os.makedirs(outdir)
    run('java', '-jar', f'{tools}/uber-apk-signer.jar', '-a', final_unsigned, '--ks', opt['--ks'], '--ksAlias', 'telecomplus', '--ksPass', 'tpstore2026', '--ksKeyPass', 'tpstore2026', '--allowResign', '-o', outdir)
    sig = glob.glob(os.path.join(outdir, '*.apk'))[0]
    shutil.copy(sig, out)
    print('OK', out, os.path.getsize(out))

main()
