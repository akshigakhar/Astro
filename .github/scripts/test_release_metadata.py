import unittest
from release_metadata import release_metadata


class ReleaseMetadataTest(unittest.TestCase):
    def test_metadata_and_monotonic_upgrades(self):
        self.assertEqual({"ASTRO_VERSION_NAME": "1.2.3", "ASTRO_VERSION_CODE": "1002003"}, release_metadata("v1.2.3"))
        tags = ["v0.0.1", "v0.1.0", "v1.0.0", "v1.2.999", "v1.3.0", "v2.0.0", "v999.999.999"]
        codes = [int(release_metadata(tag)["ASTRO_VERSION_CODE"]) for tag in tags]
        self.assertEqual(sorted(set(codes)), codes)
        self.assertLess(codes[-1], 2100000000)

    def test_invalid_or_injectable_tags(self):
        for tag in ["v0.0.0", "1.2.3", "v1.2", "v1.2.3-beta", "v01.2.3", "v1.1000.0", "v1.2.3\nBAD=value", "v1.2.3; echo secret"]:
            with self.subTest(tag=tag), self.assertRaises(ValueError):
                release_metadata(tag)


if __name__ == "__main__":
    unittest.main()
