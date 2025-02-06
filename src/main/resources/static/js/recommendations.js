function fetchRecommendations() {
  const recommendationsContainer = $('#recommendationsContainer');

  // 로딩 상태 표시
  recommendationsContainer.html(`
    <div class="col-12">
      <div class="no-recommendations">
        <div class="spinner-border text-primary" role="status">
          <span class="sr-only">Loading...</span>
        </div>
      </div>
    </div>
  `);

  axios.get('/api/survey/latest')
    .then(response => {
      recommendationsContainer.empty();
      
      // 응답에서 predictions 배열 추출
      const predictions = response.data?.predictions || [];

      // 예측 데이터가 없는 경우
      if (predictions.length === 0) {
        const noDataMessage = `
          <div class="col-12">
            <div class="no-recommendations">
              <i class="fas fa-search fa-3x mb-3" style="color: #3498db;"></i>
              <p>현재 추천 받은 여행지가 존재하지 않습니다.</p>
              <p style="margin-top: 1rem;">상단의 'AI로 여행지 추천 받기' 버튼을 눌러 새로운 추천 여행지들을 받아보고 다른 사람들에게 추천해보세요!</p>
            </div>
          </div>
        `;
        recommendationsContainer.html(noDataMessage);
        return;
      }

      // 추천 데이터가 있는 경우 표시
      predictions.forEach(prediction => {
        const recommendationElement = `
          <div class="col-md-4">
            <div class="recommendation-card">
              <div class="card-body">
                <h5 class="card-title">${prediction.place_name || '이름 없음'}</h5>
                <div class="location-info">
                  <i class="fas fa-map-marker-alt"></i>
                  <span>${prediction.address || '주소 정보 없음'}</span>
                </div>
              </div>
            </div>
          </div>
        `;
        recommendationsContainer.append(recommendationElement);
      });
    })
    .catch(error => {
      console.error("추천 여행지 데이터 처리 중 오류 발생:", error);
      
      // 404 에러(데이터 없음)와 다른 에러를 구분하여 처리
      const errorMessage = error.response?.status === 404 
        ? `
          <div class="col-12">
            <div class="no-recommendations">
              <i class="fas fa-search fa-3x mb-3" style="color: #3498db;"></i>
              <p>현재 추천 받은 여행지가 존재하지 않습니다.</p>
              <p style="margin-top: 1rem;">상단의 'AI로 여행지 추천 받기' 버튼을 눌러 새로운 추천 여행지들을 받아보고 다른 사람들에게 추천해보세요!</p>
            </div>
          </div>
        `
        : `
          <div class="col-12">
            <div class="no-recommendations">
              <i class="fas fa-exclamation-circle fa-3x mb-3" style="color: #e74c3c;"></i>
              <p>추천 여행지를 불러오는 데 실패했습니다.</p>
              <p style="margin-top: 1rem;">잠시 후 다시 시도해 주세요.</p>
            </div>
          </div>
        `;
      
      recommendationsContainer.html(errorMessage);
    });
}

// 페이지 로드 시 실행
$(document).ready(function() {
  fetchRecommendations();
});

// 전역으로 함수 노출
window.fetchRecommendations = fetchRecommendations;