#!/bin/bash

#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
#

NAME="apache-isis-demo-app"
IMAGE="apacheisis/demo-springboot:latest"
PORT=8080

echo ""
echo "killing..."
docker kill $NAME 2>/dev/null

echo ""
echo "removing ..."
docker rm $NAME 2>/dev/null

echo ""
echo "pulling..."
docker pull $IMAGE

echo ""
echo "running..."
docker run -d -p$PORT:8080 -ePROTOTYPING=true --name $NAME $IMAGE

sleep 1
echo ""
echo "status..."
docker ps

echo ""
echo "logs..."
docker logs -f $NAME
