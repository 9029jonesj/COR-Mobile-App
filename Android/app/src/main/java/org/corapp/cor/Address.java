/*
 * Copyright (C) 2015 COR Church in Irvine, CA
 *
 * Licensed under the COR License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Filename: Address.java
 * Description: Define contents of an address.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     10-Nov-15    Create file, added core functionality.
 *
 */

package org.corapp.cor;

public class Address {
    String name;
    String street;
    String cityZip;

    Address(String n, String s, String cz) {
        name = n;
        street = s;
        cityZip = cz;
    }
}
