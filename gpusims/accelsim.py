import os
from gpusims.bench import BenchmarkConfig
from pathlib import Path
import gpusims.utils as utils
from pprint import pprint


class AccelSimBenchmarkConfig(BenchmarkConfig):
    def run_input(self, inp, force=False):
        print("accelsim run:", inp)
        sim_root = Path(os.environ["SIM_ROOT"])
        setup_env = sim_root / "setup_environment"
        assert setup_env.is_file()
        utils.chmod_x(setup_env)

        executable = self.path / inp.executable
        assert executable.is_file()
        utils.chmod_x(executable)

        results_dir = self.path / "results"
        os.makedirs(str(results_dir.absolute()), exist_ok=True)
        log_file = results_dir / "log.txt"

        tmp_run = ""
        tmp_run += "source {}\n".format(str(setup_env.absolute()))
        tmp_run += "{} {}\n".format(str(executable.absolute()), inp.args)
        print(tmp_run)

        tmp_run_file = self.path / "run.tmp.sh"
        with open(str(tmp_run_file.absolute()), "w") as f:
            f.write(tmp_run)

        _, stdout, _ = utils.run_cmd(
            "bash " + str(tmp_run_file.absolute()),
            cwd=self.path,
            timeout_sec=5 * 60,
            shell=True,
        )
        print("stdout:")
        print(stdout[-100:])

        with open(str(log_file.absolute()), "w") as f:
            f.write(stdout)

        tmp_run_file.unlink()

        return
        cmd = [
            # ".",
            # str(setup_env.absolute()),
            # "&&",
            str(executable.absolute()),
            inp.args,
        ]
        # cmd = "bash -c '{}'".format(" ".join(cmd))
        cmd = " ".join(cmd)
        try:
            # run source only
            source_cmd = "bash -c '. {} > /dev/null; env'".format(
                str(setup_env.absolute())
            )
            print(source_cmd)
            _, stdout, stderr = utils.run_cmd(
                source_cmd,
                shell=True,
                # cwd=str(setup_env.parent.absolute()),
            )
            env = dict((line.split("=", 1) for line in stdout.splitlines()))
            pprint(env)
            # print(stderr)
            # return

            _, stdout, stderr = utils.run_cmd(
                cmd,
                cwd=self.path,
                timeout_sec=1 * 60,
                env=env,
                shell=True,
            )
            print("stdout:")
            print(stdout)

            print("stderr:")
            print(stderr)

            # todo: write stdout here
            # with open(str(log_file.absolute()), "r") as f:
            #     print("log file:")
            #     print(f.read())

        except utils.ExecError as e:
            raise e
