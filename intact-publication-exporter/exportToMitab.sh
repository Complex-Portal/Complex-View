MAVEN_OPTS="-Xms512m -Xmx2024m -XX:MaxPermSize=256m"

export MAVEN_OPTS


echo "MAVEN_OPTS=$MAVEN_OPTS"

echo "Publication id $1"
echo "MITAB version $2"

MAVEN_PROFILE=$3

echo "use profile ${MAVEN_PROFILE}"

mvn -U clean install -Pexport-mitab,${MAVEN_PROFILE} -DpubId=$1 -Dversion=$2 -Dmaven.repo.local=repository -Dmaven.test.skip -Ddb=oracle