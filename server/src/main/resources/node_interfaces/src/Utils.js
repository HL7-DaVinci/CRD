function getBaseUrl() {
  let baseUrl = document.querySelector("meta[name='ctx']").getAttribute("content");
  if(typeof baseUrl !== 'string') baseUrl = '/';
  if(!baseUrl.endsWith('/')) baseUrl = baseUrl + '/'
  if(!baseUrl.startsWith('/')) baseUrl = '/' + baseUrl
  return baseUrl
}

function getBackend() {
    let backend = document.querySelector("meta[name='backend']").getAttribute("content");
    if(typeof backend !== 'string') backend = '/';
    if(!backend.endsWith('/')) backend = backend + '/'
    return backend;
}

function getHostOrg() {
  let hostOrg = document.querySelector("meta[name='hostorg']").getAttribute("content");
  return hostOrg
}

module.exports = {
    getBaseUrl,
    getBackend,
    getHostOrg
}