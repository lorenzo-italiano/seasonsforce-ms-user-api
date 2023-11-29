mvn clean install

mv target/seasonsforce-ms-user-api-1.0-SNAPSHOT.jar api-image/seasonsforce-ms-user-api-1.0-SNAPSHOT.jar

cd api-image

docker build -t user-api .

cd ../minio-image

docker build -t user-minio .