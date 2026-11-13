"""Guard the documented on-device/no-network defaults at the Android package boundary."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2] / 'app/src/main'
ANDROID = '{http://schemas.android.com/apk/res/android}'
DOMAINS = {'root', 'file', 'database', 'sharedpref', 'external', 'device_root', 'device_file', 'device_database', 'device_sharedpref'}


class PrivacyManifestTest(unittest.TestCase):
    def test_app_has_no_network_or_broad_storage_permission_and_disables_backup(self):
        manifest = ET.parse(ROOT / 'AndroidManifest.xml').getroot()
        forbidden = {'android.permission.INTERNET', 'android.permission.ACCESS_NETWORK_STATE', 'android.permission.READ_EXTERNAL_STORAGE', 'android.permission.WRITE_EXTERNAL_STORAGE', 'android.permission.MANAGE_EXTERNAL_STORAGE'}
        requested = {p.attrib.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        self.assertFalse(requested & forbidden)
        app = manifest.find('application')
        self.assertEqual('false', app.attrib[ANDROID + 'allowBackup'])
        self.assertEqual('@xml/backup_rules', app.attrib[ANDROID + 'fullBackupContent'])
        self.assertEqual('@xml/data_extraction_rules', app.attrib[ANDROID + 'dataExtractionRules'])

    def test_private_data_is_excluded_from_legacy_cloud_and_device_transfer(self):
        legacy = ET.parse(ROOT / 'res/xml/backup_rules.xml').getroot()
        modern = ET.parse(ROOT / 'res/xml/data_extraction_rules.xml').getroot()
        for section in [legacy, modern.find('cloud-backup'), modern.find('device-transfer')]:
            self.assertIsNotNone(section)
            self.assertEqual(DOMAINS, {r.attrib['domain'] for r in section.findall('exclude') if r.attrib.get('path') == '.'})
            self.assertFalse(section.findall('include'))


if __name__ == '__main__':
    unittest.main()
