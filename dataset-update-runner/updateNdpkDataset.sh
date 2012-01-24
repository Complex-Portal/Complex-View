MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

echo $1
echo $2

MAVEN_PROFILE=$3

echo "use profile ${MAVEN_PROFILE}"

mvn -U clean install -Pupdate-ndpk,${MAVEN_PROFILE} -DdatasetReport=$1 -DselectionReport=$2 -Dmaven.repo.local=repository -Dmaven.test.skip -Ddb=oracle