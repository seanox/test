<p>
  <a href="https://github.com/seanox/test/pulls"
      title="Development is waiting for new issues / requests / ideas">
    <img src="https://img.shields.io/badge/development-passive-blue?style=for-the-badge">
  </a>
  <a href="https://github.com/seanox/test/issues">
    <img src="https://img.shields.io/badge/maintenance-active-green?style=for-the-badge">
  </a>
  <a href="http://seanox.de/contact">
    <img src="https://img.shields.io/badge/support-active-green?style=for-the-badge">
  </a>
</p>

# Description
AbstractSuite an extended API for JUnit tests.

A test environment is a hierarchical relation of individual tests bundled in
test suites. Test suites can thus combine individual tests and other partial
test suites.

```
Test Environment
  |
  + Suite 1
  .   |
  .   + Suite 1.1
  .   |   |
      |   + Test 1.1.1
      .   .
      .   .
      .   .
      |   + Test 1.1.n
      | 
      + Suite 1.n
      .   |
      .   + Test 1.n.1
      .   .
          .
          .
          + Test 1.n.n
```
  
In a good test environment the test can be started at any place.  
This presupposes that each test can completely prepare, use and terminate the
test environment.

AbstractSuite should help here and simplify the implementation of the test
hierarchy.

The own (Abstract)Suite is the supreme and central static component of all tests
and can use further abstraction layers and sub-suites.

```
AbstractSuite
  |
  + AbstractSubSuite (Layer 1)
  .   |
  .   + AbstractSubSuite (Layer n)
  .   |   |
      |   + Test 1
      .   .
      .   .
      .   .
      |   + Test n
      |
      + AbstractSubSuite (Layer n +1)
      .   |
      .   + Test 1
      .   .
          .
          .
          + Test n
```  

## What does AbstractSuite do?
AbstractSuite takes care of providing the test environment no matter where the
test is started.  
The mostly static architecture of JUnit provides various possibilities for
preparation and finalization. However, it is difficult to centralize and
generalize them.  
AbstractSuite helps with additional interactors (like events).  
It is possible to annotate central methods and sequences that are executed with
start and end of the test environment, start and end of test classes, or
executed before and after the execution of tests.  
Additional central I/O interfaces (e.g. System.out and System.err) are
redirected so that they can be better included in the tests.
 
## What do I have to do?
A test environment with AbstractSuite is based on hierarchical (sub)suites and
tests.  
Even if it is a static construction, it is important that all components inherit
according to this hierarchy. Thus, the test environment knows which
prerequisites are required for the execution of a test. This allows you to start
the test at any point in the test environment. 


# Licence Agreement
Seanox Software Solutions ist ein Open-Source-Projekt, im Folgenden
Seanox Software Solutions oder kurz Seanox genannt.

Diese Software unterliegt der Version 2 der GNU General Public License.

Copyright (C) 2020 Seanox Software Solutions

This program is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published by the
Free Software Foundation.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51 Franklin
Street, Fifth Floor, Boston, MA 02110-1301, USA.


# System Requirement
TODO:


# Downloads
[Seanox Test 1.2.0](https://github.com/seanox/test/raw/master/releases/seanox-test-1.2.0.zip)  
[Seanox Test Sources 1.2.0](https://github.com/seanox/test/raw/master/releases/seanox-test-1.2.0-src.zip)  


# Installation
TODO:

# Changes (Change Log)
## 1.2.0 20200420 (summary of the current version)  
CR: Pattern: Update of ACCESS_LOG_STATUS  
CR: OutputFacadeStream Write: Add a flush  
CR: OutputFacadeStream Capture: Added various await methods  
CR: Build: Update to use CHANGES as release notes  
CR: Project: Update Hamcrest version 2.2  
CR: Project: Update JUnit version 4.13  

[Read more](https://raw.githubusercontent.com/seanox/test/master/CHANGES)


# Contact
[Issues](https://github.com/seanox/test/issues)  
[Requests](https://github.com/seanox/test/pulls)  
[Mail](http://seanox.de/contact)


# Thanks!
<img src="https://raw.githubusercontent.com/seanox/seanox/master/sources/resources/images/thanks.png">

[JetBrains](https://www.jetbrains.com/?from=seanox)  
Sven Lorenz  
Andreas Mitterhofer  
[novaObjects GmbH](https://www.novaobjects.de)  
Leo Pelillo  
Gunter Pfannm&uuml;ller  
Annette und Steffen Pokel  
Edgar R&ouml;stle  
Michael S&auml;mann  
Markus Schlosneck  
[T-Systems International GmbH](https://www.t-systems.com)
