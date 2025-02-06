// 전역 변수 선언
let userNo = null;
let currentPage = 1;
const itemsPerPage = 9;
let allFavorites = [];

// DOM 로드 시 초기화
document.addEventListener('DOMContentLoaded', function () {
    const userNoField = document.getElementById('userNoField');
    userNo = userNoField && userNoField.value ? parseInt(userNoField.value) : null;
    
    if (userNo) {
        loadFavorites();
    } else {
        console.warn('사용자 정보를 찾을 수 없습니다.');
        showLoginPrompt();
    }
});

// 즐겨찾기 데이터 로드
function loadFavorites() {
    if (userNo === null) {
        console.warn('사용자 정보가 없습니다.');
        return;
    }

    axios.get(`/api/favorites/${userNo}`)
        .then(response => {
            // 리다이렉션이 필요한 경우
            if (response.status === 302 && response.data.redirect) {
                window.location.href = response.data.redirect;
                return;
            }

            if (response.status === 204) {
                displayFavorites([]);
                return;
            }

            if (response.data && response.data.status === 'success') {
                allFavorites = response.data.favorites;
                displayFavorites(allFavorites);
                setupPagination(allFavorites.length);
            } else {
                throw new Error('데이터 형식이 올바르지 않습니다.');
            }
        })
        .catch(error => {
            // 리다이렉션이 필요한 경우
            if (error.response && error.response.status === 302 && error.response.data.redirect) {
                window.location.href = error.response.data.redirect;
                return;
            }
            console.error('즐겨찾기 목록을 불러오는 중 오류가 발생했습니다:', error);
            showErrorMessage();
        });
}

// 즐겨찾기 목록 표시
function displayFavorites(favorites) {
    const container = document.getElementById('favorites-list');
    container.innerHTML = '';

    if (!favorites || favorites.length === 0) {
        container.innerHTML = `
            <div class="text-center my-5">
                <h3>즐겨찾기한 장소가 없습니다.</h3>
                <p>관심있는 장소를 즐겨찾기해보세요!</p>
            </div>`;
        document.getElementById('pagination').innerHTML = '';
        return;
    }

    // 현재 페이지에 해당하는 항목만 표시
    const startIndex = (currentPage - 1) * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const currentFavorites = favorites.slice(startIndex, endIndex);

    const row = document.createElement('div');
    row.className = 'row';

    currentFavorites.forEach(favorite => {
        const col = document.createElement('div');
        col.className = 'col-md-4 mb-4';

        const createdAt = new Date(favorite.createdAt);
        const formattedDate = createdAt.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });

        col.innerHTML = `
            <div class="card h-100">
                <div class="card-body">
                    <h5 class="card-title">${escapeHtml(favorite.placeName)}</h5>
                    <p class="card-text">${escapeHtml(favorite.address)}</p>
                    <p class="card-text">
                        <small class="text-muted">등록일: ${formattedDate}</small>
                    </p>
                </div>
                <div class="card-footer bg-transparent d-flex justify-content-center">
                    <button class="btn btn-info btn-sm" onclick="openMapModal('${favorite.address}')">
                        상세보기
                    </button>
                    <button class="btn btn-danger btn-sm ml-2" onclick="deleteFavorite(${favorite.favoriteId})">
                        삭제
                    </button>
                </div>
            </div>
        `;
        row.appendChild(col);
    });

    container.appendChild(row);
}

// 페이지네이션 설정
function setupPagination(totalItems) {
    const totalPages = Math.ceil(totalItems / itemsPerPage);
    const paginationContainer = document.getElementById('pagination');
    paginationContainer.innerHTML = '';

    if (totalPages <= 1) {
        return;
    }

    const pagination = document.createElement('ul');
    pagination.className = 'pagination';

    // 이전 페이지 버튼
    const prevLi = document.createElement('li');
    prevLi.className = `page-item ${currentPage === 1 ? 'disabled' : ''}`;
    prevLi.innerHTML = `
        <a class="page-link" href="#" aria-label="Previous">
            <span aria-hidden="true">&laquo;</span>
        </a>
    `;
    prevLi.onclick = () => {
        if (currentPage > 1) {
            currentPage--;
            displayFavorites(allFavorites);
            setupPagination(totalItems);
        }
    };
    pagination.appendChild(prevLi);

    // 페이지 번호 버튼
    for (let i = 1; i <= totalPages; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${currentPage === i ? 'active' : ''}`;
        li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
        li.onclick = () => {
            currentPage = i;
            displayFavorites(allFavorites);
            setupPagination(totalItems);
        };
        pagination.appendChild(li);
    }

    // 다음 페이지 버튼
    const nextLi = document.createElement('li');
    nextLi.className = `page-item ${currentPage === totalPages ? 'disabled' : ''}`;
    nextLi.innerHTML = `
        <a class="page-link" href="#" aria-label="Next">
            <span aria-hidden="true">&raquo;</span>
        </a>
    `;
    nextLi.onclick = () => {
        if (currentPage < totalPages) {
            currentPage++;
            displayFavorites(allFavorites);
            setupPagination(totalItems);
        }
    };
    pagination.appendChild(nextLi);

    paginationContainer.appendChild(pagination);
}

// 즐겨찾기 삭제
function deleteFavorite(favoriteId) {
    if (!confirm('정말로 이 즐겨찾기를 삭제하시겠습니까?')) return;

    axios.delete(`/api/favorites/${userNo}/${favoriteId}`)
        .then(response => {
            // 리다이렉션이 필요한 경우
            if (response.status === 302 && response.data.redirect) {
                window.location.href = response.data.redirect;
                return;
            }

            if (response.data && response.data.status === 'success') {
                alert('즐겨찾기가 삭제되었습니다.');
                if (allFavorites.length % itemsPerPage === 1 && currentPage === Math.ceil(allFavorites.length / itemsPerPage)) {
                    currentPage = Math.max(1, currentPage - 1);
                }
                loadFavorites();
            }
        })
        .catch(error => {
            // 리다이렉션이 필요한 경우
            if (error.response && error.response.status === 302 && error.response.data.redirect) {
                window.location.href = error.response.data.redirect;
                return;
            }
            console.error('즐겨찾기 삭제 중 오류가 발생했습니다:', error);
            alert('삭제 중 문제가 발생했습니다.');
        });
}

// 지도 모달 열기
function openMapModal(address) {
    $('#mapModal').modal('show');

    $('#mapModal').on('shown.bs.modal', function () {
        const mapContainer = document.getElementById('map');
        mapContainer.innerHTML = '';

        const mapOptions = {
            center: new kakao.maps.LatLng(37.5665, 126.9780),
            level: 4
        };

        const map = new kakao.maps.Map(mapContainer, mapOptions);
        const geocoder = new kakao.maps.services.Geocoder();

        geocoder.addressSearch(address, function (result, status) {
            if (status === kakao.maps.services.Status.OK) {
                const coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                map.setCenter(coords);

                const marker = new kakao.maps.Marker({
                    map: map,
                    position: coords
                });

                const infowindow = new kakao.maps.InfoWindow({
                    content: `<div style="padding:5px; text-align: center;">${address}</div>`
                });
                infowindow.open(map, marker);

                const bounds = new kakao.maps.LatLngBounds();
                bounds.extend(coords);
                map.setBounds(bounds);

                document.getElementById('directionsBtn').onclick = () => {
                    window.open(`https://map.kakao.com/link/to/${address},${result[0].y},${result[0].x}`, '_blank');
                };
            } else {
                alert('해당 주소를 찾을 수 없습니다.');
            }
        });
    });
}

// XSS 방지를 위한 HTML 이스케이프 함수
function escapeHtml(unsafe) {
    return unsafe
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// 에러 메시지 표시
function showErrorMessage() {
    const container = document.getElementById('favorites-container');
    container.innerHTML = `
        <div class="alert alert-danger text-center my-5" role="alert">
            즐겨찾기 목록을 불러오는 중 문제가 발생했습니다. 
            <br>잠시 후 다시 시도해주세요.
        </div>`;
}