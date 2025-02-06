
document.addEventListener('DOMContentLoaded', () => {

    const apiKey = decodeURIComponent(window.apiKey);
    const apiUrl = 'http://apis.data.go.kr/B551011/KorService1/areaBasedSyncList1';
    const detailApiUrl = 'http://apis.data.go.kr/B551011/KorService1/detailCommon1';
    const container = document.querySelector('.card-section');

    const areaCodeMap = {
        1: '서울',
        2: '인천',
        3: '대전',
        4: '대구',
        5: '광주',
        6: '부산',
        7: '울산',
        8: '세종',
        31: '경기',
        32: '강원',
    };

    const getRandomElement = (arr) => arr[Math.floor(Math.random() * arr.length)];

    const fetchApiData = async (url, params) => {
        try {
            const response = await axios.get(url, { params });
            return response?.data?.response?.body?.items?.item || [];
        } catch (error) {
            console.error('API 호출 중 오류 발생:', error);
            return [];
        }
    };

    const updateHeroSection = (areaName) => {
        const heroSection = document.querySelector('.hero-section h1');
        heroSection.textContent = `${areaName} 지역을 추천해드려요!`;
    };

    const renderCarousel = (items) => {
        if (items.length === 0) {
            container.innerHTML = '<p class="text-center">이미지가 포함된 여행지 데이터를 찾을 수 없습니다.</p>';
            return;
        }

        const carouselItems = items
            .map(
                (item, index) => `
            <div class="carousel-item ${index === 0 ? 'active' : ''}">
                <div class="d-flex flex-column align-items-center">
                    <img src="${item.firstimage}" 
                         class="d-block travel-image" 
                         style="width: 900px; height: 506px; object-fit: cover;" 
                         alt="${item.title}" 
                         data-contentid="${item.contentid}">
                    <div class="text-center mt-3">
                        <h4>${item.title}</h4>
                        <p>${item.addr1 || '주소 정보 없음'}</p>
                    </div>
                </div>
            </div>`
            )
            .join('');

        container.innerHTML = `
            <div id="travelCarousel" class="carousel slide" data-ride="carousel" data-interval="8000">
                <div class="carousel-inner">${carouselItems}</div>
                <a class="carousel-control-prev" href="#travelCarousel" role="button" data-slide="prev">
                    <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                    <span class="sr-only">Previous</span>
                </a>
                <a class="carousel-control-next" href="#travelCarousel" role="button" data-slide="next">
                    <span class="carousel-control-next-icon" aria-hidden="true"></span>
                    <span class="sr-only">Next</span>
                </a>
            </div>
        `;

        document.querySelectorAll('.travel-image').forEach((img) => {
            img.addEventListener('click', async (e) => {
                const contentId = e.target.getAttribute('data-contentid');
                await showModal(contentId);
            });
        });
    };

    const fetchTravelData = async () => {
        const areaCode = getRandomElement(Object.keys(areaCodeMap));
        const areaName = areaCodeMap[areaCode];
        updateHeroSection(areaName);

        const params = {
            serviceKey: apiKey,
            numOfRows: 20,
            pageNo: getRandomElement([1, 2, 3, 4, 5]),
            MobileOS: 'ETC',
            MobileApp: 'Travelpicker',
            contentTypeId: 12,
            areaCode,
            showflag: 1,
            _type: 'Json',
        };

        const items = await fetchApiData(apiUrl, params);
        const filteredItems = items.filter((item) => item.firstimage);
        renderCarousel(filteredItems);
    };

    const showModal = async (contentId) => {
        const params = {
            serviceKey: apiKey,
            _type: 'Json',
            contentId,
            numOfRows: 1,
            pageNo: 1,
            MobileOS: 'ETC',
            MobileApp: 'Travelpicker',
            defaultYN: 'Y',
            overviewYN: 'Y',
        };

        const details = await fetchApiData(detailApiUrl, params);
        const detail = Array.isArray(details) ? details[0] : details;

        if (!detail) {
            alert('해당 여행지 정보를 불러올 수 없습니다.');
            return;
        }

        const overview = detail.overview
            ? detail.overview.replace(/\n/g, '<br>')
            : '상세 정보가 제공되지 않습니다.';

        const modal = document.getElementById('infoModal');
        modal.querySelector('.modal-title').textContent = detail.title || '여행지 정보';
        modal.querySelector('.modal-body').innerHTML = `<p><strong>소개:</strong> ${overview}</p>`;
        $('#infoModal').modal('show');
    };

    fetchTravelData();
});
