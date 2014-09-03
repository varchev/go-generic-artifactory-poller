#Go Generic Artifactory Registry Poller

A [Go](http://www.go.cd) plugin that polls a Artifactory repository

[![Build Status](https://travis-ci.org/varchev/go-generic-artifactory-poller.svg?branch=master)](https://travis-ci.org/varchev/go-generic-artifactory-poller)

Introduction
------------
This is a [package material](http://www.thoughtworks.com/products/docs/go/current/help/package_material.html) plugin for [Go](http://www.go.cd). It is currently capable of polling [Artifactory](http://www.jfrog.com/artifactory/) repositories.

The behaviour and capabilities of the plugin are determined to a significant extent by that of the package material extension point in Go. Be sure to read the package material documentation before using this plugin.

This plugin polls artifactory repositories using its [REST API](http://www.jfrog.com/confluence/display/RTF/Artifactory+REST+API). It relies on the convention that version number is part of the file path.

Installation
------------
Just drop [go-generic-artifactory-poller.jar](https://github.com/varchev/go-generic-artifactory-poller/releases) into plugins/external directory and restart Go. More details [here](http://www.thoughtworks.com/products/docs/go/current/help/plugin_user_guide.html)

Repository definition
---------------------
![Add an Artifactory repository][1]

Artifactory repository URL must be a valid http or https URL. For example, specify the URL as http://artifactory.example.com/artifactory/. The plugin will try to access the URL to report successful connection.

Package definition
------------------
Click check package to make sure the plugin understands what you are looking for. Note that the version constraints are AND-ed if both are specified.

![Define a package as material for a pipeline][2]

Published Environment Variables
-------------------------------
The following information is made available as environment variables for tasks:

    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LABEL
    GO_REPO_<REPO-NAME>_<PACKAGE-NAME>_REPO_URL
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_REPO_ID
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_PACKAGE_PATH
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_PACKAGE_ID
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LOCATION
    GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_VERSION

The LOCATION variable points to a downloadable url.

Downloading the Package
-----------------------
To download the package locally on the agent you could use [curl](http://curl.haxx.se/) (or wget) task like this:

                <exec command="cmd" >
                <arg>/c</arg>
                <arg>curl -o /path/to/package.zip $GO_PACKAGE_REPONAME_PKGNAME_LOCATION</arg>
                </exec>

When the task executes on the agent, the environment variables get subsituted and the package gets downloaded.

[1]: doc/artifactory-repo.png  "Define Artifactory Package Repository"
[2]: doc/generic-artifactory-add-pkg.png  "Define package as material for a pipeline"