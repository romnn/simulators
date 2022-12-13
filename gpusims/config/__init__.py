import yaml
import gpusims.config.gpgpusim  # noqa: F401
import gpusims.config.tejas  # noqa: F401
import gpusims.config.multi2sim  # noqa: F401
import gpusims.config.macsim  # noqa: F401
from collections import namedtuple

Config = namedtuple("Config", ["name", "path", "spec"])
# class Config:
#    def __init__(self, name, path):
#        if path is not None:
#            assert path.is_dir()
#        self.name = name
#        self.path = path
#
#    def __repr__(self):
#        return "{}({})".format(self.__class__.__name__, self.path)


def parse_configs(path):
    configs = {}
    with open(str(path.absolute()), "r") as f:
        try:
            configs_yaml = yaml.load(f)
        except TypeError:
            configs_yaml = yaml.load(f, Loader=yaml.FullLoader)

        # pprint(configs_yaml)
        for name, config in configs_yaml.items():
            configs[name.lower()] = Config(
                name=name,
                path=path.parent / config["path"],
                spec=config["spec"],
            )
    return configs
