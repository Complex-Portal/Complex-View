MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

echo "Log directory $1"
echo "IMEx central username $2"
echo "IMEx central password $3"
echo "IMEx central endpoint $4"
echo "Path to the generated keystore $6"
echo "Password of the generated keystore $7"

MAVEN_PROFILE=$5

echo "use profile ${MAVEN_PROFILE}"

mvn -U clean install -Pexec,${MAVEN_PROFILE} -Dupdate.log.directory=$1 -Dic.username=$2 -Dic.password=$3 -Dic.endpoint=$4 -Djavax.net.ssl.keyStore=$6 -Djavax.net.ssl.keyStorePassword=$7 -Dmaven.repo.local=repository -Dmaven.test.skip -Ddb=oracle