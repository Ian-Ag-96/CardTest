# CardTest
An application that allows users to create and modify card information.


# Prerequisites
- Ensure at least Java version 17 is installed on the host machine to enable the build process for the jar file.
- You may optionally have maven (latest) installed as well.
- The application uses port 8081 and the database uses port 5431. Ensure the ports are free and accept tcp connections. If not you will need to modify the application.properties file to reflect the database port you wish to use, the Dockerfile to modify the exposed application port, and the docker-compose.yml file to modify both the database and application ports to the desired ones.
- Ensure docker is installed on the host machine.
- Ensure the host machine is connected to the internet.


# Configuration
- Clone the repository from the command line if git is installed in your system using 'git clone https://github.com/Ian-Ag-96/CardTest.git' or any other cloning mechanism you may wish to use.
- Navigate to the directory where the project resides and open the command line/terminal.
- Run the command 'mvn clean install -DskipTests' if maven is already installed or 'mvnw clean install -DskipTests' - (Windows) or './mvnw clean install -DskipTests' (Linux) if it is not installed. We are skipping tests because we want to only build the jar file. A container called 'card_test_tests' will be created later on for specifically running tests.
- After the jar file has been built successfully, ensure docker is running first then run the command 'docker-compose up --build' to download the necessary files, handle configurations, create all the containers, and start the application, including running tests. This may take some time as maven will have to be installed including its dependencies to handle tests.
- For specific cases you may use the following commands:
	- 'docker-compose up database' to only start the database and initialize the data
	- 'docker-compose up app' to start the application and database without running tests
	- 'docker-compose up test' to run the tests only

# Users
There are preloaded users in the database from the sql file 'card-test.sql':

	- Admin user
		email: 'adminone@administrators.com'
		password: 'admin1234'

	- Member user
		email: 'memberfour@allmembers.com'
		password: 'member1234'

# Documentation
The documentation can be found on this link: https://documenter.getpostman.com/view/41252801/2sAYdZvaLy
The documentation is done using Postman.
Requests can be found in the file 'CardTest.postman_collection'


# Extras
An API, though out of scope, for creating users is also included in the service.
