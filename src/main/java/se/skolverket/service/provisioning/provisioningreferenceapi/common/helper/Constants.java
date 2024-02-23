package se.skolverket.service.provisioning.provisioningreferenceapi.common.helper;

public final class Constants {

  private Constants() {
  }

  // Config
  public static final String CONFIG_KEY_MONGO_HOST = "REF_API_MONGO_HOST";
  public static final String CONFIG_KEY_MONGO_HOST_DEFAULT = "localhost";
  public static final String CONFIG_KEY_MONGO_PORT = "REF_API_MONGO_PORT";
  public static final Integer CONFIG_KEY_MONGO_PORT_DEFAULT = 27017;
  public static final String CONFIG_KEY_VALIDATION_BASE_URI = "VALIDATION_BASE_URI";
  public static final String CONFIG_KEY_VALIDATION_BASE_URI_DEFAULT = "https://no-configured-url.com";
  public static final String CONFIG_SS12000_AUTH_JWKS_URI = "SS12000_AUTH_JWKS_URI";
  public static final String CONFIG_SS12000_AUTH_IGNORE_JWT_WEBHOOKS = "SS12000_AUTH_IGNORE_JWT_WEBHOOKS";
  public static final String CONFIG_AUTH_JWT_CLAIM_LOCATION = "AUTH_JWT_CLAIM_LOCATION";
  public static final String CONFIG_AUTH_JWT_CLAIM_ORGANIZATION_ID = "AUTH_JWT_CLAIM_ORGANIZATION_ID";
  public static final String CONFIG_AUTH_JWT_CLAIM_LOCATION_DEFAULT = "http://localhost:8888";

  public static final String CONFIG_HTTP_TRUST_ALL = "HTTP_TRUST_ALL";
  public static final String CONFIG_HTTP_PROXY_HOST = "HTTP_PROXY_HOST";
  public static final String CONFIG_HTTP_PROXY_PORT = "HTTP_PROXY_PORT";
  public static final Integer CONFIG_HTTP_PROXY_PORT_DEFAULT = 3128;
  public static final String CONFIG_HTTP_PROXY_EXCEPTIONS = "HTTP_PROXY_EXCEPTIONS";

  //Query Params
  public static final String QP_PAGE_TOKEN = "pageToken";
  public static final String QP_LIMIT = "limit";
  public static final String QP_AFTER = "after";
  public static final String QP_BEFORE = "before";
  public static final String QP_ENTITIES = "entities";
  public static final String QP_META_CREATED_BEFORE = "meta.created.before";
  public static final String QP_META_CREATED_AFTER = "meta.created.after";
  public static final String QP_META_MODIFIED_BEFORE = "meta.modified.before";
  public static final String QP_META_MODIFIED_AFTER = "meta.modified.after";

  //Path parameters
  public static final String PP_SUBSCRIPTION_ID = "subscriptionId";

  //PageToken keys
  public static final String PT_CURSOR = "cursor";
  public static final String PT_INDEX = "index";
  public static final String PT_REQUEST = "req";

  //Mongodb $ Prefix
  public static final String DB_OBJECT_ID = "$oid";
  public static final String DB_GREATER_THAN = "$gt";
  public static final String DB_LESS_THAN = "$lt";
  public static final String DB_LESS_THAN_OR_EQUALS = "$lte";
  public static final String DB_MATCHES_ANY = "$in";
  public static final String DB_AND = "$and";
  public static final String DB_DATE = "$date";

  //Bson
  public static final String BSON_ID = "_id";

  //Properties
  public static final String ID = "id";
  public static final String META = "meta";
  public static final String CREATED = "created";
  public static final String MODIFIED = "modified";
  public static final String RESOURCE = "resource";
  public static final String RESOURCE_TYPE = "resourceType";
  public static final String RESOURCE_TYPES = "resourceTypes";
  public static final String DELETED_ENTITY_ID = "deletedEntityId";
  public static final String EXPIRES = "expires";
  public static final String NAME = "name";
  public static final String TARGET = "target";


  // JWT
  public static final String JWT_REQUESTED_ACCESS_TYPE = "ss12000-api";
}
