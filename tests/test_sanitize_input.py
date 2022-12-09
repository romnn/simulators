import unittest
import gpusims.utils as utils


class TestSanitizeInput(unittest.TestCase):
    def test_escapes_slashes(self):
        self.assertEqual(utils.slugify("-m 32 -f ./data.sh"), "m-32-f-datash")
        self.assertEqual(
            utils.slugify("-m 32 --m32 -f ./data.sh --log-file ./more.log"),
            "m-32-m32-f-datash-log-file-morelog",
        )


if __name__ == "__main__":
    unittest.main()
