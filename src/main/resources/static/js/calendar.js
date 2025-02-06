document.addEventListener('DOMContentLoaded', function() {
    let currentDate = new Date();
    
	// 현재 시각 업데이트 함수
	function updateCurrentTime() {
	    const now = new Date();
	    let hours = now.getHours();
	    const minutes = String(now.getMinutes()).padStart(2, '0');
	    const period = hours >= 12 ? 'PM' : 'AM';
	    
	    // 12시간 형식으로 변환
	    hours = hours % 12;
	    hours = hours ? hours : 12; // 0시를 12시로 표시
	    hours = String(hours).padStart(2, '0');
	    
	    document.querySelector('.time-display .hours').textContent = hours;
	    document.querySelector('.time-display .minutes').textContent = minutes;
	    document.querySelector('.time-display .period').textContent = period;
	}

	// 1초마다 시간 업데이트
	setInterval(updateCurrentTime, 1000);

	// 초기 시간 설정
	updateCurrentTime();
	
    // 공휴일 정의
    const holidays = {
        '1-1': '신정',
        '3-1': '삼일절',
        '5-5': '어린이날, 부처님 오신날',
        '6-6': '현충일',
        '8-15': '광복절',
        '10-3': '개천절',
        '10-9': '한글날',
        '12-25': '크리스마스'
    };

    // 2025년 음력 공휴일 
    const lunarHolidays2025 = {
        '1-27': '임시공휴일',
        '3-3': '대체공휴일(삼일절)',
        '1-28': '공휴일(설날)',
        '1-29': '설날',
        '1-30': '공휴일(설날)',
        '10-6': '추석',
        '10-7': '공휴일(추석)',
        '10-8': '공휴일(추석)'
    };

    const calendar = {
        init() {
            this.date = currentDate;
            this.render();
            this.bindEvents();
        },

        isHoliday(year, month, day) {
            const dateKey = `${month + 1}-${day}`;
            
            // 양력 공휴일과 음력 공휴일을 모두 체크
            let holidayName = holidays[dateKey];
            
            // 2024년인 경우 음력 공휴일도 체크
            if (year === 2025) {
                holidayName = holidayName || lunarHolidays2025[dateKey];
            }
            
            return holidayName || '';
        },

        render() {
            const year = this.date.getFullYear();
            const month = this.date.getMonth();

            // Update title
            document.querySelector('.calendar-title').textContent =
                `${year}년 ${month + 1}월`;

            // Clear previous days
            const daysContainer = document.getElementById('calendarDays');
            daysContainer.innerHTML = '';

            // Calculate days
            const firstDay = new Date(year, month, 1);
            const lastDay = new Date(year, month + 1, 0);
            const startingDay = firstDay.getDay();

            // Previous month's days
            const prevMonthDays = new Date(year, month, 0).getDate();
            for (let i = 0; i < startingDay; i++) {
                const day = document.createElement('div');
                day.className = 'calendar-day other-month';
                day.textContent = prevMonthDays - startingDay + i + 1;
                daysContainer.appendChild(day);
            }

            // Current month's days
            const today = new Date();
            for (let i = 1; i <= lastDay.getDate(); i++) {
                const day = document.createElement('div');
                day.className = 'calendar-day';
                
                // 오늘 날짜 표시
                if (year === today.getFullYear() &&
                    month === today.getMonth() &&
                    i === today.getDate()) {
                    day.classList.add('today');
                }
                
                const dayNum = document.createElement('span');
                dayNum.textContent = i;
                day.appendChild(dayNum);

                // 주말 및 공휴일 처리
                const dayOfWeek = new Date(year, month, i).getDay();
                const holidayName = this.isHoliday(year, month, i);

                if (dayOfWeek === 0 || holidayName) { // 일요일이거나 공휴일
                    day.style.color = '#ff4d4d';
                } else if (dayOfWeek === 6) { // 토요일
                    day.style.color = '#4d79ff';
                }

                // 공휴일 표시
                if (holidayName) {
                    day.classList.add('holiday');
                    // data 속성으로 공휴일 이름 저장
                    day.dataset.holiday = holidayName;
                    
                    // 마우스 이벤트로 툴팁 제어
                    day.addEventListener('mouseenter', function(e) {
                        // 기존 툴팁 제거
                        const existingTooltip = this.querySelector('.holiday-tooltip');
                        if (existingTooltip) {
                            existingTooltip.remove();
                        }
                        
                        // 새 툴팁 생성
                        const tooltip = document.createElement('span');
                        tooltip.className = 'holiday-tooltip';
                        tooltip.textContent = this.dataset.holiday;
                        this.appendChild(tooltip);
                    });

                    day.addEventListener('mouseleave', function(e) {
                        const tooltip = this.querySelector('.holiday-tooltip');
                        if (tooltip) {
                            tooltip.remove();
                        }
                    });
                }

                daysContainer.appendChild(day);
            }

            // Next month's days
            const remainingDays = 42 - (startingDay + lastDay.getDate());
            for (let i = 1; i <= remainingDays; i++) {
                const day = document.createElement('div');
                day.className = 'calendar-day other-month';
                day.textContent = i;
                daysContainer.appendChild(day);
            }
        },

        bindEvents() {
            document.getElementById('prevMonth').addEventListener('click', () => {
                this.date.setMonth(this.date.getMonth() - 1);
                this.render();
            });

            document.getElementById('nextMonth').addEventListener('click', () => {
                this.date.setMonth(this.date.getMonth() + 1);
                this.render();
            });
        }
    };

    calendar.init();
});