package be.cytomine.image.server.resolvers

import grails.converters.JSON

/**
 * Cytomine @ GIGA-ULG
 * User: stevben
 * Date: 13/01/11
 * Time: 10:08
 */
class DjatokaResolver extends Resolver {

  public DjatokaResolver() {
    super()
  }

  public String getThumbUrl(String baseUrl, String imagePath) {
    args.put("rft_id", imagePath)
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getRegion")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    args.put("svc.scale", "192")
    return toURL(baseUrl)
  }

  public String getMetaDataURL(String baseUrl, String imagePath) {
    args.put("rft_id", imagePath)
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getMetadata")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    return toURL(baseUrl)
  }
  public String getCropURL(String baseUrl, String imagePath, int topLeftX, int topLeftY, int width, int height, int zoom) {
    args.put("rft_id", imagePath)
    args.put("url_ver", "Z39.88-2004")
    args.put("svc_id", "info:lanl-repo/svc/getRegion")
    args.put("svc_val_fmt", "info:ofi/fmt:kev:mtx:jpeg2000")
    args.put("svc.format", "image/jpeg")
    args.put("svc.region", topLeftY+ ","+topLeftX+","+height+","+width)
    args.put("svc.level",zoom)
    return toURL(baseUrl)
  }
}