export const FETCH_CRD = 'FETCH_CRD';

export function fetchCrdResponse(payload){
    return {
        type: FETCH_CRD,
        data: payload
    };
}