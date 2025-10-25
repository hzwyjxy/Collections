#!/bin/bash
echo "Starting Maven with custom configuration..."

# 设置Maven路径
export MAVEN_HOME="/d/Trae area/apache-maven-3.9.9"
export PATH="$MAVEN_HOME/bin:$PATH"

# 设置本地仓库路径
export MAVEN_OPTS="-Dmaven.repo.local=/d/Trae area/.m2/repository"

echo "Maven Home: $MAVEN_HOME"
echo "Local Repository: /d/Trae area/.m2/repository"
echo ""

# 执行Maven命令
if [ $# -eq 0 ]; then
    echo "Usage: ./mvn-run.sh [maven-commands]"
    echo "Example: ./mvn-run.sh clean install"
    echo "Example: ./mvn-run.sh compile"
    echo "Example: ./mvn-run.sh test"
else
    echo "Executing: mvn $@"
    mvn "$@"
fi

echo ""
echo "Maven execution completed."