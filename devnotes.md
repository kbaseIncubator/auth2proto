Developer notes
===============

Returned data structures should not include a top level 'error' field. This is
reserved for returning errors in JSON.

Templates are mustache templates.

Exception mapping
-----------------

in us.kbase.auth2.exceptions  
AuthException and subclasses other than the below - 400  
AuthenticationException and subclasses - 401  
UnauthorizedException and subclasses - 403  
NoDataException and subclasses - 404  

Anything else is mapped to 500.

Current endpoints
-----------------

/admin/localaccount?admin=&lt;some name&gt;  
create a local account. The admin param is a temporary placeholder.

/localaccount/login  
login to a local account. Stores a cookie with a token.

/tokens  
list and create tokens

/api/legacy/KBase/Sessions/Login  
the legacy KBase API

/api/legacy/globus  
the legacy globus API. Endpoints are /goauth/token and /users.




