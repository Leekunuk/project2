$(document).ready(function() {
    var $newPassword = $('#newPassword');
    var $confirmPassword = $('#confirmPassword');
    var $currentPassword = $('#currentPassword');
    var $submitBtn = $('#editProfileSubmitBtn');
    var $passwordHelp = $('#passwordHelp');
    var $confirmPasswordHelp = $('#confirmPasswordHelp');
    var passwordValid = true;
    var confirmPasswordValid = true;
    var currentPasswordValid = false;
    
    // 초기값 저장
    var initialName = $('#name').val();
    var initialPhone = $('#phone').val();
    var hasChanges = false;

    // 입력 필드 값 변경 감지
    $('#name, #phone').on('input', function() {
        checkForChanges();
    });

    function checkForChanges() {
        var nameChanged = $('#name').val() !== initialName;
        var phoneChanged = $('#phone').val() !== initialPhone;
        var passwordChanged = $newPassword.val().length > 0;
        
        hasChanges = nameChanged || phoneChanged || passwordChanged;
        updateSubmitButtonState();
    }

    // 현재 비밀번호 검증
    $currentPassword.on('input', function() {
        currentPasswordValid = $(this).val().length > 0;
        updateSubmitButtonState();
    });

    // 새 비밀번호 유효성 검사 함수
    function validateNewPassword() {
        var password = $newPassword.val();
        var currentPw = $currentPassword.val();
        
        if (password.length === 0) {
            $passwordHelp.text('');
            passwordValid = true;
            $confirmPassword.prop('disabled', true);
            $confirmPasswordHelp.text('');
            checkForChanges();
            return true;
        }
        
        if (password === currentPw) {
            $passwordHelp.text('현재 비밀번호와 동일합니다.')
                        .removeClass('text-muted text-success')
                        .addClass('text-danger');
            passwordValid = false;
            $confirmPassword.prop('disabled', true);
            $confirmPasswordHelp.text('');
            return false;
        }
        
        $confirmPassword.prop('disabled', false);
        
        var lengthValid = password.length >= 8;
        var hasUpperCase = /[A-Z]/.test(password);
        var hasLowerCase = /[a-z]/.test(password);
        var hasNumbers = /\d/.test(password);
        var hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

        var message = [];
        if (!lengthValid) message.push("최소 8자 이상");
        if (!hasUpperCase) message.push("대문자");
        if (!hasLowerCase) message.push("소문자");
        if (!hasNumbers) message.push("숫자");
        if (!hasSpecialChar) message.push("특수문자(!@#$%^&*(),.?\":{}|<>)");

        if (message.length > 0) {
            $passwordHelp.html("비밀번호는 다음을 포함해야 합니다:<br>" + message.join(", "))
                        .removeClass('text-muted text-success')
                        .addClass('text-danger');
            passwordValid = false;
        } else {
            $passwordHelp.text("사용 가능한 비밀번호입니다 :)")
                        .removeClass('text-muted text-danger')
                        .addClass('text-success');
            passwordValid = true;
        }

        validateConfirmPassword();
        checkForChanges();
        updateSubmitButtonState();
        return passwordValid;
    }

    $currentPassword.on('input', function() {
        currentPasswordValid = $(this).val().length > 0;
        if ($newPassword.val().length > 0) {
            validateNewPassword();
        }
        updateSubmitButtonState();
    });

    function validateConfirmPassword() {
        var password = $newPassword.val();
        var confirmPassword = $confirmPassword.val();
        
        if (password.length === 0 || password === $currentPassword.val()) {
            $confirmPasswordHelp.text('');
            confirmPasswordValid = true;
            return true;
        }
        
        if (confirmPassword.length === 0) {
            $confirmPasswordHelp.text('비밀번호 확인을 입력해주세요.')
                              .removeClass('text-muted text-success')
                              .addClass('text-danger');
            confirmPasswordValid = false;
        } else if (password !== confirmPassword) {
            $confirmPasswordHelp.text('비밀번호가 일치하지 않습니다 :(')
                              .removeClass('text-muted text-success')
                              .addClass('text-danger');
            confirmPasswordValid = false;
        } else {
            $confirmPasswordHelp.text('비밀번호가 일치합니다 :)')
                              .removeClass('text-muted text-danger')
                              .addClass('text-success');
            confirmPasswordValid = true;
        }

        updateSubmitButtonState();
        return confirmPasswordValid;
    }

    function updateSubmitButtonState() {
        $submitBtn.prop('disabled', !hasChanges || !(passwordValid && confirmPasswordValid && currentPasswordValid));
    }

    $newPassword.attr('placeholder', '8자 이상, 대/소문자, 숫자, 특수문자 포함');
    $newPassword.on('input', validateNewPassword);
    $confirmPassword.on('input', validateConfirmPassword);
    
    $('#phone').on('input', function() {
        var phoneValue = $(this).val().replace(/[^0-9]/g, '');
        if (phoneValue.length < 4) {
            $(this).val(phoneValue);
        } else if (phoneValue.length < 7) {
            $(this).val(phoneValue.replace(/(\d{3})(\d{1,4})/, '$1-$2'));
        } else {
            $(this).val(phoneValue.replace(/(\d{3})(\d{1,4})(\d{1,4})/, '$1-$2-$3'));
        }
    });

    $('#editProfileForm').on('submit', function(e) {
        e.preventDefault();
        
        if (!validateNewPassword() || !validateConfirmPassword() || !currentPasswordValid) {
            alert('입력 정보를 다시 확인해주세요.');
            return;
        }

        var formData = $(this).serialize();
        
        $.ajax({
            url: $(this).attr('action'),
            type: 'POST',
            data: formData,
            success: function(response) {
                if (response.success) {
                    alert(response.message);
                    window.location.reload();
                } else {
                    alert(response.message);
                }
            },
            error: function() {
                alert('서버 오류가 발생했습니다. 다시 시도해주세요.');
            }
        });
    });
});