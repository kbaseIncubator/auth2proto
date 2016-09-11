Auth2 TODO list
===============

Update core services and SDK modules
------------------------------------
*Note:* not in auth team scope unless specified

Need to use updated server stubs & auth clients, use tokens for magic user
accounts and tests if any, allow setting auth service url

* Shock & Awe - Rich
* Perl auth & server stubs - Keith
* Handle service & manager - Keith
* Narrative (Lua) - Bill R. (external to auth team)
* kb_sdk
  * Tests support token vs uid/pwd & setting auth url
  * Recompile & test all SDK modules
* NJSW
* User Profile
  * Update to get and set user name & email from and to auth service
* Service wizard
* Narrative Method Store
* Data Import Export
* Search

Auth service work
-----------------
* UI (significant time sink per Bill & Erik)
  * Probably means altering server endpoints in concert with UI development
* 150-200 TODOs in the codebase on average
* Read through all prototype code and convert to production worthy
* A code review by Steve Chan wouldn't be a bad idea
* Tests
  * With mock services for globus and google
* Documentation
  * User documentation and education (probably need doc team help here)
  * Login & signup very different
* Admin functionality
  * Find users
  * revoke single / user's / all tokens
  * Bootstrap root user
  * Create admins role
  * View / modify server config
  * Disable account (revoke all tokens & prevent logins)
  * Force pwd reset for local accounts (per user and all)
  * Reset local account pwd
  * Admin checking for all /admin functions
* /me UI allows updating user record
* API
  * Introspect token (e.g. not the legacy apis, provide complete info)
  * /user/<name> - get user details
  * /me
* Memory based data storage
* Test mode
  * test apis for user creation & admin
  * auto configure server for ease of use
* User import - import current Globus users
* Deploy
  * Dockerization

### Potential work
* Support user lookup by identity provider & id for bulk upload (permitted role)

External dependencies
---------------------
* JGI updates kbase<->JGI account linking
* JGI stops using uid/pwd to login for jgidm account

Future work
-----------

### 3rd party developers acting on behalf of users (e.g. JGI, sequencing centers)
* OAuth2 endpoint
* -or-
* (simpler) verify user name via KBase login

### Scoped tokens
* Mainly for SDK jobs
* Scoped to read/write specific workspaces only, no other system rights
* Could be scoped for other things if necessary
