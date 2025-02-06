// weather.js
function loadWeatherData() {
    const weatherBox = document.querySelector('.weather-widget'); // 클래스명 수정
    if (!weatherBox) return; // 요소가 없을 경우 처리
    
    const refreshButton = weatherBox.querySelector('.refresh-weather');
    if (!refreshButton) return; // 요소가 없을 경우 처리
    
    // 새로고침 버튼 비활성화 및 로딩 표시
    refreshButton.disabled = true;
    refreshButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            function(position) {
                const latitude = position.coords.latitude;
                const longitude = position.coords.longitude;

                // 날씨 정보를 가져오는 API 호출
                $.get(`/weather?lat=${latitude}&lon=${longitude}`, function(data) {
                    if (data.error) {
                        showWeatherError(data.error);
                    } else {
                        updateWeatherBox(data);
                    }
                })
                .fail(function(error) {
                    showWeatherError('날씨 정보를 가져오는데 실패했습니다.');
                })
                .always(function() {
                    // 새로고침 버튼 복원
                    refreshButton.disabled = false;
                    refreshButton.innerHTML = '<i class="fas fa-sync-alt"></i>';
                });
            },
            function(error) {
                let errorMessage;
                switch(error.code) {
                    case error.PERMISSION_DENIED:
                        errorMessage = "위치 정보 접근을 허용해주세요.";
                        break;
                    case error.POSITION_UNAVAILABLE:
                        errorMessage = "위치 정보를 가져올 수 없습니다.";
                        break;
                    case error.TIMEOUT:
                        errorMessage = "요청이 시간 초과되었습니다.";
                        break;
                    default:
                        errorMessage = "알 수 없는 오류가 발생했습니다.";
                }
                showWeatherError(errorMessage);
                // 새로고침 버튼 복원
                refreshButton.disabled = false;
                refreshButton.innerHTML = '<i class="fas fa-sync-alt"></i>';
            }
        );
    } else {
        showWeatherError("이 브라우저에서는 위치 정보를 지원하지 않습니다.");
        // 새로고침 버튼 복원
        refreshButton.disabled = false;
        refreshButton.innerHTML = '<i class="fas fa-sync-alt"></i>';
    }
}

function showWeatherError(message) {
    const weatherBox = document.querySelector('.weather-widget'); // 클래스명 수정
    if (!weatherBox) return;
    const weatherInfo = weatherBox.querySelector('.weather-info');
    if (!weatherInfo) return;
    
    weatherInfo.innerHTML = `
        <p class="feature-text text-danger">${message}</p>
    `;
}

function getWeatherIcon(sky, precipitation) {
    // 강수 상태 먼저 체크 (비, 눈 등이 있는 경우 우선처리)
    switch(precipitation) {
        case '비':
            return 'fa-cloud-rain';
        case '비/눈':
            return 'fa-cloud-sleet';
        case '눈':
            return 'fa-snowflake';
        case '소나기':
            return 'fa-cloud-showers-heavy';
        case '없음':
            // 강수가 없는 경우 하늘 상태에 따라 처리
            switch(sky) {
                case '맑음':
                    return 'fa-sun';
                case '구름많음':
                    return 'fa-cloud-sun';
                case '흐림':
                    return 'fa-cloud';
                default:
                    return 'fa-sun'; // 기본값
            }
        default:
            return 'fa-sun'; // 기본값
    }
}

function updateWeatherBox(data) {
    const weatherBox = document.querySelector('.weather-widget');
    if (!weatherBox) return;
    const weatherInfo = weatherBox.querySelector('.weather-info');
    if (!weatherInfo) return;
	
	const weatherIcon = getWeatherIcon(data.sky, data.precipitation);

    weatherInfo.innerHTML = `
        <div class="weather-main">
            <div class="weather-temp">
                <span class="temp-value">${data.temperature}</span>
                <span class="temp-unit">°C</span>
            </div>
            <div class="weather-condition">
                <i class="fas ${weatherIcon}"></i>
                <span>${data.weather}</span>
            </div>
        </div>
        <div class="weather-details">
            <div class="weather-detail">
                <i class="fas fa-tint"></i>
                <span>${data.rainProbability}%</span>
            </div>
            <div class="weather-detail">
                <i class="fas fa-map-marker-alt"></i>
                <span>${data.location}</span>
            </div>
            <div class="weather-detail">
                <i class="fas fa-calendar"></i>
                <span>${data.date}</span>
            </div>
        </div>
    `;
}

// DOM이 완전히 로드된 후에 실행
document.addEventListener('DOMContentLoaded', function() {
    loadWeatherData();
    
    // 새로고침 버튼 클릭 이벤트 추가
    const refreshButton = document.querySelector('.refresh-weather');
    if (refreshButton) {
        refreshButton.addEventListener('click', loadWeatherData);
    }
});