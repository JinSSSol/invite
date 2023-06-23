package com.study.api.group.dto.redis;

import com.study.api.group.dto.InviteForm;
import com.study.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Invite {

    private String userEmail;
    private String userName;
    private String userPhone;
    private String senderEmail;
    private Long groupId;

    public static Invite from(InviteForm.New form) {
        return Invite.builder()
            .userEmail(form.getUserEmail())
            .userName(form.getUserName())
            .userPhone(form.getPhone())
            .build();
    }

    public static Invite from(User user) {
        return Invite.builder()
            .userEmail(user.getUserEmail())
            .userName(user.getUserName())
            .userPhone(user.getPhone())
            .build();
    }

    public void setSenderEmailAndGroupId(String email, Long groupId) {
        this.senderEmail = email;
        this.groupId = groupId;
    }
}
