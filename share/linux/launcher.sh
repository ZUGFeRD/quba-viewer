#!/usr/bin/env bash
#
# Alternative application launcher for Linux systems.
#
# Workaround for a possible error on loading libcef.so.
# libcef.so: cannot allocate memory in static TLS block
#
# see https://youtrack.jetbrains.com/issue/JBR-4721/JCEF-fails-with-cannot-allocate-memory-in-static-TLS-block
#
# -------------------------------------------------------------
#
# Copyright (c) 2024-2025 Andreas Rudolph <andy@openindex.de>.
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

set -e

BASE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LIB_DIR="$( dirname "${BASE_DIR}" )"
SCRIPT_NAME="$(basename "$0")"
LAUNCHER_NAME="${SCRIPT_NAME%.*}"
#echo "${LAUNCHER_NAME}"

# Preloading "libcef.so" as a workaround.
export LD_PRELOAD="${LIB_DIR}/lib/runtime/lib/libcef.so"

# Running the application launcher.
exec "${BASE_DIR}/${LAUNCHER_NAME}"
