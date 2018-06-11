Configuration

This is an example to explain how Pact works. The best way to publish and consum contracts is using a pact-broker, which is an application that provides a repository where publish and consum
the contracts. It has a dashboard where you can check the contracts that you published.

To configure Pact-Broker we need to install also postgres
https://github.com/DiUS/pact_broker-docker/blob/master/POSTGRESQL.md


docker run --name pactbroker-db -e POSTGRES_PASSWORD=exito -e POSTGRES_USER=admin -e PGDATA=/var/lib/postgresql/data/pgdata -v /var/lib/postgresql/data:/var/lib/postgresql/data -d postgres

Connect to the postgress container to execute some commands
docker run -it --link pactbroker-db:postgres --rm postgres sh -c 'exec psql -h "$POSTGRES_PORT_5432_TCP_ADDR" -p "$POSTGRES_PORT_5432_TCP_PORT" -U admin'

CREATE USER pactbrokeruser WITH PASSWORD 'TheUserPassword';
CREATE DATABASE pactbroker WITH OWNER pactbrokeruser;
GRANT ALL PRIVILEGES ON DATABASE pactbroker TO pactbrokeruser;

-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Add the pgadmin to see the databases and schemas:

docker run -v ~/pgadmin4/data:/home/pgadmin/.pgadmin -p 5050:5050 -e PGADMIN_USER=test@test.com -e PGADMIN_PASSWORD=123456 meedan/pgadmin

get ip container to connect to postgres:

docker inspect container_ID  | grep "IPAddress"

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
Pact-Broker

docker run --name pactbroker --link pactbroker-db:postgres -e PACT_BROKER_DATABASE_USERNAME=pactbrokeruser -e PACT_BROKER_DATABASE_PASSWORD=TheUserPassword -e PACT_BROKER_DATABASE_HOST=postgres -e PACT_BROKER_DATABASE_NAME=pactbroker -d -p 80:80 dius/pact-broker

Note:
The Docker file of the pact broker exposes the port 80. If we want to user another posrt from our local machine we should write 5060:80, that means that we maping our port 5060
with the port 80 of the container. So to access the broker dashboard we should write in the browser http://localhost:5060

------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

Test Consumer

./gradlew test ->will create the pact in /build/pacts if everything runs smoothly

./gradlew pactPublish ->publish the pact in the pact broker. You will be able to see it in the broker dashboard(http://localhost)

Test Provider

./gradle test -> will verify the pact in the pact broker.

Link with a good example
https://medium.com/techbeatscorner/consumer-driven-contracts-using-a-pact-broker-b1743c2f8fe5






