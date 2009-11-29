namespace java com.geocoord.thrift.services

include "com.geocoord.thrift.data.thrift"

service CoverageService {
  com.geocoord.thrift.data.CoverageResponse getCoverage(1:com.geocoord.thrift.data.CoverageRequest request)  
}

service DataService {
  //com.geocoord.thrift.data.DataResponse lookup(1:com.geocoord.thrift.data.DataRequest) throws (1:com.geocoord.thrift.data.GeoCoordException e)
}

service SearchService {
}

service UserService {
  com.geocoord.thrift.data.User load(1:string key) throws (1:com.geocoord.thrift.data.GeoCoordException e)
  com.geocoord.thrift.data.User store(1:com.geocoord.thrift.data.User user) throws (1:com.geocoord.thrift.data.GeoCoordException e)  
}