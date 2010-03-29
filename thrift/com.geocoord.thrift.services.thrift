namespace java com.geocoord.thrift.services

include "com.geocoord.thrift.data.thrift"

service PoolableThriftService {
  //  i32 healthcheck()
}

service CoverageService extends PoolableThriftService {
  com.geocoord.thrift.data.CoverageResponse getCoverage(1:com.geocoord.thrift.data.CoverageRequest request)  
}

service DataService extends PoolableThriftService {
  //com.geocoord.thrift.data.DataResponse lookup(1:com.geocoord.thrift.data.DataRequest) throws (1:com.geocoord.thrift.data.GeoCoordException e)
}

service SearchService extends PoolableThriftService {
}

service UserService extends PoolableThriftService {
  com.geocoord.thrift.data.User load(1:string key) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.User store(1:com.geocoord.thrift.data.User user) throws (1:com.geocoord.thrift.data.GeoCoordException e)  
}

/**
 * Service used to access Layer objects persistently stored.
 */
service LayerService extends PoolableThriftService {
  com.geocoord.thrift.data.LayerCreateResponse   create(1:com.geocoord.thrift.data.LayerCreateRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.LayerRetrieveResponse retrieve(1:com.geocoord.thrift.data.LayerRetrieveRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.LayerUpdateResponse   update(1:com.geocoord.thrift.data.LayerUpdateRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.LayerRemoveResponse   remove(1:com.geocoord.thrift.data.LayerRemoveRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)      
}

service AtomService extends PoolableThriftService {
  com.geocoord.thrift.data.AtomCreateResponse   create(1:com.geocoord.thrift.data.AtomCreateRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.AtomRetrieveResponse retrieve(1:com.geocoord.thrift.data.AtomRetrieveRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.AtomUpdateResponse   update(1:com.geocoord.thrift.data.AtomUpdateRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.AtomRemoveResponse   remove(1:com.geocoord.thrift.data.AtomRemoveRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)      
}

/**
 * An activity service records the changes and makes sure
 * the data gets correctly indexed.
 */
service ActivityService {
  void record(1:com.geocoord.thrift.data.ActivityEvent event) throws (1:com.geocoord.thrift.data.GeoCoordException e)
}

service CentroidService extends PoolableThriftService {
  com.geocoord.thrift.data.CentroidResponse search(1:com.geocoord.thrift.data.CentroidRequest request) throws (1:com.geocoord.thrift.data.GeoCoordException e)
}
