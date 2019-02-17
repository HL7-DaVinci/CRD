function getBaseUrl() {
  let baseUrl = document.querySelector("meta[name='ctx']").getAttribute("content");
  if(typeof baseUrl !== 'string') baseUrl = '/';
  if(!baseUrl.endsWith('/')) baseUrl = baseUrl + '/'
  if(!baseUrl.startsWith('/')) baseUrl = '/' + baseUrl
  return baseUrl
}

module.exports.getBaseUrl = getBaseUrl;