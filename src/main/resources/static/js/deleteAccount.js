$(document).ready(function() {
    // 회원 탈퇴 폼 제출
    $(document).on('submit', '#deleteAccountForm', function(e) {
        e.preventDefault();

        const password = $('#deletePassword').val();
        if (!password) {
            $('#deletePasswordError').text('비밀번호를 입력해주세요.');
            return;
        }

        $.ajax({
            url: '/mypage/delete',
            type: 'POST',
            data: { password: password },
            success: function(response) {
                if (response.success) {
                    alert('회원 탈퇴가 완료되었습니다.');
                    window.location.href = '/';
                } else {
                    $('#deletePasswordError').text(response.message);
                }
            },
            error: function() {
                $('#deletePasswordError').text('처리 중 오류가 발생했습니다. 다시 시도해주세요.');
            }
        });
    });

    // 모달이 닫힐 때 폼 초기화
    $(document).on('hidden.bs.modal', '#deleteAccountModal', function() {
        $('#deleteAccountForm')[0].reset();
        $('#deletePasswordError').text('');
    });
});