const https = require('https')

const VSAC_AUTH_URL = "https://vsac.nlm.nih.gov/vsac/ws"
const VSAC_CONTENT_URL = "https://vsac.nlm.nih.gov/vsac/svs"

module.exports.getTicketGrantingTicket = async function getTicketGrantingTicket(username, password) {
  https.
};