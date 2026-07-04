package com.code.musicapp.entity;

/**
 * Trang thai bai hat. Mac dinh APPROVED vi hien tai nhom CHUA lam workflow duyet bai
 * (theo quyet dinh don gian hoa chi 2 role ADMIN/USER). De san field nay de neu sau
 * nay them tinh nang "USER upload -> cho ADMIN duyet" thi khong phai doi schema/migration.
 */
public enum SongStatus {
    PENDING,   // cho duyet (chua dung toi trong scope hien tai)
    APPROVED,  // hien thi cong khai, nghe duoc
    REJECTED   // bi tu choi, khong hien thi
}
