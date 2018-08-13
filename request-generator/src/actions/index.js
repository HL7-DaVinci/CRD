export const FETCH_CRD = 'FETCH_CRD';

export function fetchCrdResponse(payload){
    console.log("request bois");
    return {
        type: FETCH_CRD,
        data: payload
    };
}