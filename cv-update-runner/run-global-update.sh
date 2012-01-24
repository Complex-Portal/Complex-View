MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

echo $1

MAVEN_PROFILE=$2

echo "use profile ${MAVEN_PROFILE}"

mvn -U clean install -Pexec,${MAVEN_PROFILE} -Dupdate.report=$1 -Dmaven.repo.local=repository -Dmaven.test.skip -Ddb=oracle