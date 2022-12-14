import yaml
import gpusims.config.gpgpusim  # noqa: F401
import gpusims.config.tejas  # noqa: F401
import gpusims.config.multi2sim  # noqa: F401
import gpusims.config.macsim  # noqa: F401
from collections import namedtuple

Config = namedtuple("Config", ["key", "name", "path", "spec"])


def parse_configs(path):
    configs = {}
    with open(str(path.absolute()), "r") as f:
        try:
            configs_yaml = yaml.load(f)
        except TypeError:
            configs_yaml = yaml.load(f, Loader=yaml.FullLoader)

        # pprint(configs_yaml)
        for key, config in configs_yaml.items():
            configs[key.lower()] = Config(
                key=key.lower(),
                name=config["name"],
                path=path.parent / config["path"],
                spec=config["spec"],
            )
    return configs
