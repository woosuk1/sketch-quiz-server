package com.itcen.whiteboardserver.security.principal;

import com.itcen.whiteboardserver.member.enums.MemberRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomPrincipal implements UserDetails {
    private final Long    id;
    private final String  email;
    private final String  nickname;
    private final String password; // 비밀번호는 필요 없지만 UserDetails를 구현하기 위해 필요
//    private final Collection<? extends GrantedAuthority> authorities;

    private final Set<MemberRole> memberRoles;

    // MemberRole을 받아서 내부적으로 List<GrantedAuthority> 로 변환하는 생성자
//    public CustomPrincipal(Long id, String email, String nickname, String password, Collection<? extends GrantedAuthority> authorities) {
//    public CustomPrincipal(Long id, String email, String nickname, String password, MemberRole role) {
    public CustomPrincipal(Long id, String email, String nickname, String password, Set<MemberRole> memberRoles) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
//        this.authorities = authorities;
//        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        this.memberRoles = memberRoles;
    }

    // UserDetails를 구현하는 메서드들...
//    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return memberRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
    }
    @Override public String getUsername()                 { return email; }
    @Override public String getPassword()                 { return password; }
    @Override public boolean isAccountNonExpired()        { return true; }
    @Override public boolean isAccountNonLocked()         { return true; }
    @Override public boolean isCredentialsNonExpired()    { return true; }
    @Override public boolean isEnabled()                  { return true; }

    // 토큰 내부에 쓸 getter
    public Long getId()          { return id; }
//    public String getEmail()     { return email; }
    public String getNickname()  { return nickname; }
    public Set<MemberRole> getRoles() { return memberRoles;}

}

