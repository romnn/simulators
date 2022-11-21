#!/bin/bash

echo ""
#echo "This will guide you through the installation of GPU Ocelot required to run GPU Tejas"
#echo ""

#--- validating the arguments
if [ $# -lt 1 ];
then
	echo "ERROR: Missing arguments"
	echo "Usage: ./setup_gputejas.sh setup/genTrace/run/clean/make-jar [benchmark .o files path]"
	echo "exiting..."
	echo ""
	exit 1	
fi


if [ $1 = 'make-jar' ]
then
	echo "ANT BUILD"
	echo "- - - - - - - - - - - - - - - -"
	echo ""

	echo "cd gputejas"
	cd gputejas

	echo ""
	echo "ant clean"
	ant clean
	
	echo ""
	echo "ant"
	ant
	
	echo ""
	echo "ant make-jar"
	ant make-jar

	echo ""
	echo "--- Done with making the jar ---"
	exit 0
fi


if [ $1 = 'run' ]
then
	echo "RUNNING THE BENCHMARK"
	echo "- - - - - - - - - - - - - - - -"
	echo ""

	echo "Enter the path of config file (absolute path): "
	read configPath

	echo "Enter the path of output file (with name): "
	read outputFile
	#configPath=gputejas/src/simulator/config/config.xml
	#outputFile=output.txt
	threadNum=`grep -o '<MaxNumJavaThreads>.*</MaxNumJavaThreads>' $configPath | cut -d'<' -f 2 | cut -d'>' -f 2`

	kernels=`ls $threadNum/hashfile_* | wc -l`

	echo "java -jar jars/GPUTejas.jar $configPath $outputFile . $kernels"
	
	if !(java -jar gputejas/jars/GPUTejas.jar $configPath $outputFile . $kernels) then
		echo "Problem with running the benchmark, try generating the traces again, exiting..."
		echo ""
		exit 1
	fi

	exit 0
fi


if [ $1 = 'genTrace' ]
then

	echo ""
	echo "GENERATING THE TRACES"
	echo "- - - - - - - - - - - - - - - -"
	
	# if [ $# -lt 2 ];
	# then
	# 	echo "Please pass the benchmark path containing .o files"
	# 	echo "exiting..."
	# 	echo ""
	# 	exit 1
	# fi
	
	echo ""
	echo "-----------------Cleaning the temporary files-----------"
	rm *.txt *.o tmp tracegen 2>/dev/null

	echo "Benchmark path containing .o files : $2"
	read bench_path
	echo $bench_path
	
	echo "Enter the path of config file: "
	read configPath
	#configPath=gputejas/src/simulator/config/config.xml
	threadNum=`grep -o '<MaxNumJavaThreads>.*</MaxNumJavaThreads>' $configPath | cut -d'<' -f 2 | cut -d'>' -f 2`

	#--- removing the $threadNum directory if present
	rm -rf $threadNum 2>/dev/null 
	
   	echo "Please enter the arguments to run the benchmark"
   	read args	
	echo $args
	# args=512 2 2 /home/khushal/Downloads/rodinia_2.1/data/hotspot/temp_512 /home/khushal/Downloads/rodinia_2.1/data/hotspot/power_512 output.out
	
	#--- Compiling Tracegen.cpp ---

	echo ""
	echo "----------Compiling Tracegen.cpp----------"
	echo "g++ -std=c++0x Tracegen.cpp -c -I ."
	g++-4.8 -std=c++0x Tracegen.cpp -c -I .

	#bench_path=/home/khushal/Downloads/rodinia_2.1/cuda/hotspot
	#--- generating tracegen executable ---
	echo ""
	echo "----------Generating tracegen executable----------"
	echo "g++ -o tracegen $2/*.o Tracegen.o -locelot -ltinfo"
	#g++-4.8 -o tracegen $bench_path/*.o Tracegen.o -locelot -ltinfo -L/usr/lib/x86_64-linux-gnu/ -lcudart
	g++-4.4 -o tracegen $bench_path/*.o Tracegen.o -locelot -ltinfo 
	#--- generating traces ---
	echo ""
	echo "----------Generating traces----------"
	echo "./tracegen $args $threadNum"
	./tracegen $args $threadNum

	
	#--- checking number of kernels ---
	kernels=`grep "KERNEL START" 0.txt | wc -l`

	#--- creating a new folder & moving the text files	
	echo "mkdir $threadNum"
	mkdir $threadNum

	echo "mv *.txt $threadNum"
	mv *.txt $threadNum

	#--- trace simplifier ---
	echo ""
	echo "----------Simplifying the traces----------"
	echo "java -jar gputejas/TraceSimplifier.jar $configPath tmp . $kernels"

	if !(java -jar gputejas/Tracesimplifier.jar $configPath tmp . $kernels) then
		echo "Problem with simplifying the traces, please try again, exiting..."
		echo ""
		exit 1
	fi


	echo ""
    	echo "--- Generated the traces, please run the benchmark using './setup_gputejas.sh run' ---"
        exit 0
fi


if [ $1 = 'setup' ]
then
	echo ""
	echo "SETTING UP THE RESOURCES"
	echo "- - - - - - - - - - - - - - - -"

	echo ""
	echo "Please make sure to have Java installed before continuing"
	echo "press any key to continue: "
	read key

	#--- Knowing the architecture
	echo "Please enter '32' for 32-bit architecture or '64' for 64-bit architecture"
	read archi

	if [[ $archi != '32' && $archi != '64' ]]
	then
		echo "Wrong architecture passed, exiting..."
		echo ""
		exit 1
	fi

	
 	#--- extracting ocelot ---
  	#echo "Enter path of ocelot help files(.tar.gz): "
  	# read ocelot_path

   # 	if !(tar -xvf $ocelot_path) then
   #      	echo "Error in extracting ocelot help files, exiting..."
   #      	exit 1
   # 	fi
   
       
	#--- update repository  ---
	echo ""
	echo "----------Let us first update the repository----------"
	echo "Press 'y' to continue---"
	read op

	if [[ $op != 'y' && $op != 'Y' ]]
	then
		echo "exiting..."
		echo ""
		exit 1
	fi

	echo "sudo apt-get update"
	sudo apt-get update


	#------ Installing required softwares------
	echo ""
	echo "-----Installing required softwares-----"
	echo "The following softwares will be installed: "
	# echo "1. nvidia-cuda-toolkit"
	echo "2. ant"
	echo "3. g++-4.4"
	
	echo "Press 'y' to continue"
	read op

	if [[ $op != 'y' && $op != 'Y' ]]
	then
		echo "exiting..."
		echo ""
		exit 1
	fi


	# #--- install nvidia-cuda-toolkit  ---
	# echo ""
	# echo "----------Installing nvidia-cuda-toolkit----------"
	
	# echo "sudo apt-get install nvidia-cuda-toolkit"
	# if !(sudo apt-get install nvidia-cuda-toolkit) then
	# 	echo "problem while installing 'nvidia-cuda-toolkit', exiting..."
	# 	echo ""
	# 	exit 1
	# fi
	sudo apt-get remove nvidia-cuda-toolkit
	sudo rm /usr/bin/gcc
	sudo ln -s gcc-4.4 gcc
	sudo cp -r cuda-toolkit/bin/ /usr/bin/ 
	sudo cp -r cuda-toolkit/include/crt /usr/include/
	sudo cp -r cuda-toolkit/include/*.h /usr/include/
	sudo cp -r nvidia-cuda-toolkit/ /usr/lib/
	#--- install ant  ---
	echo ""
	echo "----------Installing ant----------"
	
	echo "sudo apt-get install ant"
	if !(sudo apt-get install ant) then
		echo "problem while installing 'ant', exiting..."
		echo ""
		exit 1
	fi


	#--- install g++-4.8  ---
	echo ""
	echo "----------Installing g++-4.8----------"
	
	echo "sudo apt-get install g++"
	if !(sudo apt-get install g++-4.4) then
		echo "sudo apt-get install python-software-properties"
		if !(sudo apt-get install python-software-properties) then
			exit 1
		fi
		
		echo "sudo add-apt-repository ppa:ubuntu-toolchain-r/test"
		if !(sudo add-apt-repository ppa:ubuntu-toolchain-r/test) then
			exit 1
		fi
		
		echo "sudo apt-get update"
		if !(sudo apt-get update) then
			exit 1
		fi	
		
		echo "sudo apt-get install g++-4.8"
		if !(sudo apt-get install g++-4.4) then
			exit 1
		fi
	fi


	#--- Copying .so files to the respective directories
	if [ $archi = 32 ] 
	then
		echo ""
		echo "----------Copying required .so files to the respective directories----------"
		echo "The following files will be copied:"
		echo "1. libocelot.so 		-to- 	/usr/lib/"
		echo "2. libtinfo.so 			-to- 	/usr/lib/i386-linux-gnu/"
		echo "3. libboost_thread.so.1.54.0 	-to- 	/usr/lib/i386-linux-gnu/"
		echo "4. libboost_system.so.1.54.0 	-to- 	/usr/lib/i386-linux-gnu/"
		echo "5. libz.so 			-to- 	/lib/i386-linux-gnu/"
		echo "6. libGLEW.so.1.10		-to-	/usr/lib/i386-linux-gnu/"

		echo "Press 'y' to continue"
		read op

		if [[ $op != 'y' && $op != 'Y' ]]
		then
			echo "exiting..."
			echo ""
			exit 1
		fi

		sudo cp so_files_32bit/libocelot.so '/usr/lib/libocelot.so'
		sudo rm /usr/lib/i386-linux-gnu/libtinfo.so 2>/dev/null
		sudo cp so_files_32bit/libtinfo.so '/usr/lib/i386-linux-gnu/libtinfo.so'
		sudo cp so_files_32bit/libboost_thread.so.1.54.0 '/usr/lib/i386-linux-gnu/libboost_thread.so.1.54.0'
		sudo cp so_files_32bit/libboost_system.so.1.54.0 '/usr/lib/i386-linux-gnu/libboost_system.so.1.54.0'
		sudo cp so_files_32bit/libz.so.1.2.8 '/lib/i386-linux-gnu/libz.so'
		sudo rm /usr/lib/i386-linux-gnu/libGLEW.so.1.10 2>/dev/null
		sudo cp so_files_32bit/libGLEW.so.1.10 '/usr/lib/i386-linux-gnu/libGLEW.so.1.10'
	fi

	
	if [ $archi = 64 ]
	then
		echo ""
		echo "----------Copying required .so files to the respective directories----------"
		echo "The following files will be copied:"
		echo "1. libocelot.so 		-to- 	/usr/lib/"
		echo "2. libtinfo.so 			-to- 	/usr/lib/x86_64-linux-gnu/"
		echo "3. libboost_thread.so.1.54.0 	-to- 	/usr/lib/x86_64-linux-gnu/"
		echo "4. libboost_system.so.1.54.0 	-to- 	/usr/lib/x86_64-linux-gnu/"
		echo "5. libz.so 			-to- 	/lib/x86_64-linux-gnu/"
		echo "6. libGLEW.so.1.10		-to-	/usr/lib/x86_64-linux-gnu/"

		echo "Press 'y' to continue"
		read op

		if [[ $op != 'y' && $op != 'Y' ]]
		then
			echo "exiting..."
			echo ""
			exit 1
		fi

		sudo cp so_files_64bit/libocelot.so '/usr/lib/libocelot.so'
		sudo rm /usr/lib/x86_64-linux-gnu/libtinfo.so 2>/dev/null
		sudo cp so_files_64bit/libtinfo.so '/usr/lib/x86_64-linux-gnu/libtinfo.so'
		sudo cp so_files_64bit/libboost_thread.so.1.54.0 '/usr/lib/x86_64-linux-gnu/libboost_thread.so.1.54.0'
		sudo cp so_files_64bit/libboost_system.so.1.54.0 '/usr/lib/x86_64-linux-gnu/libboost_system.so.1.54.0'
		sudo cp so_files_64bit/libz.so.1.2.8 '/lib/x86_64-linux-gnu/libz.so'
		sudo rm /usr/lib/x86_64-linux-gnu/libGLEW.so.1.10 2>/dev/null
		sudo cp so_files_64bit/libGLEW.so.1.10 '/usr/lib/x86_64-linux-gnu/libGLEW.so.1.10'
	fi


	#--- ant build ---
	echo ""
	echo "-----ant build-----"
	echo "cd gputejas"
	cd gputejas

	echo "ant clean"
	ant clean

	echo "ant"
	ant

	echo "ant make-jar"
	ant make-jar


	echo ""
	echo "--- Done with the setup!!! ---"
	exit 0
fi

if [ $1 = 'clean' ]
then
    echo "CLEANING..."
    echo "rm *.txt *.o tracegen"
    rm *.txt *.o tmp tracegen 2>/dev/null
    echo "---- Done ---"
    exit 0
fi
