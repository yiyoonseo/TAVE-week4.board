import http from 'k6/http';
import { check } from 'k6';

const BASE = __ENV.BASE_URL || 'http://localhost:8080';

export const options = {
    vus: 50,          // 동시 사용자 50명
    duration: '30s',  // 30초 동안
};

// 테스트 전에 게시글 100개 생성
export function setup() {
    const ids = [];
    for (let i = 0; i < 100; i++) {
        const res = http.post(`${BASE}/api/posts`,
            JSON.stringify({ title: `k6 글 ${i}`, content: '부하 테스트 본문', author: 'k6' }),
            { headers: { 'Content-Type': 'application/json' } });
        ids.push(res.headers['Location'].split('/').pop());
    }
    return { ids };
}

// 본 테스트: 랜덤 id로 단건 조회 (캐시가 걸린 API)
export default function (data) {
    const id = data.ids[Math.floor(Math.random() * data.ids.length)];
    const res = http.get(`${BASE}/api/posts/${id}`);
    check(res, { 'status 200': (r) => r.status === 200 });
}