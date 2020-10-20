# spring-issue

The main purpose of this repo is to reproduce problem with filter.

After change in undertow: 

https://github.com/undertow-io/undertow/pull/880/files 

https://issues.redhat.com/browse/UNDERTOW-1197

## Issue

When I am using my custom filter and have security lib on path my application doesn't return response body for async endpoint.

## How to reproduce?

You can run test which reproduce this issue, or make a request to an endpoint `GET /response`
