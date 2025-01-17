/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.integration;

import de.adorsys.ledgers.app.BaseContainersTest;
import de.adorsys.ledgers.app.LedgersApplication;
import de.adorsys.ledgers.app.TestDBConfiguration;
import de.adorsys.ledgers.app.it_stages.ManagementStage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.List;

import static de.adorsys.ledgers.app.Const.*;//NOPMD
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"testcontainers-it", "sandbox"})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = LedgersApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {TestDBConfiguration.class},
        initializers = { PaymentIT.Initializer.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserManagementIT extends BaseContainersTest<ManagementStage, ManagementStage, ManagementStage> {
    private static final String BRANCH = "UA_735297";
    private static final String CUSTOMER_LOGIN = "newcutomer";
    private static final String CUSTOMER_EMAIL = "newcutomer@mail.de";
    private static final String NEW_IBAN = "UA379326228389769852388931161";
    @Test
    void deleteUserAsAdmin() {
        given().obtainTokenFromKeycloak(ADMIN_LOGIN, ADMIN_PASSWORD)
                .getUserIdByLogin(PSU_LOGIN);
        when().deleteUser();
        then().getAllUsers()
                .path("login", (List<String> logins) -> assertThat(logins).doesNotContain(PSU_LOGIN));
    }
    @Test
    void addNewStaffUserAndDepositCash() {
        addNewTpp();
        var amount = "10000.00";
        given()
                .obtainTokenFromKeycloak(TPP_LOGIN_NEW, TPP_PASSWORD)
                .createNewUserAsStaff(CUSTOMER_LOGIN, CUSTOMER_EMAIL, BRANCH)
                .createNewAccountForUser("new_account.json", NEW_IBAN)
                .accountByIban(NEW_IBAN);
        when()
                .depositCash("deposit_amount.json", amount);
        then()
                .getAccountDetails()
                .path("balances.amount.amount[0]", am -> assertThat(am.toString()).isEqualTo(amount));
    }
    @Test
    void testTppUpdatesHimself() {
        addNewTpp();
        String newUserLogin = "newtpplogin";
        given()
                .obtainTokenFromKeycloak(TPP_LOGIN_NEW, TPP_PASSWORD)
                .getUserIdByLogin(TPP_LOGIN_NEW)
                .modifyUser("update_tpp_user.json", newUserLogin, TPP_EMAIL_NEW, BRANCH);
        when()
                .obtainTokenFromKeycloak(newUserLogin, TPP_PASSWORD);
        then()
                .readUserFromDb(newUserLogin);
    }
    @Test
    void testUpdatePassword() {
        addNewTpp();
        String newPassword = "hello";
        when().obtainTokenFromKeycloak(ADMIN_LOGIN, ADMIN_PASSWORD);
        then().changePasswordBranch(BRANCH, newPassword)
                .obtainTokenFromKeycloak(TPP_LOGIN_NEW, newPassword);
    }
    @Test
    void testUpdateSelf() {
        String newTppLogin = "new-tpp-login";
        String newTppEmail = "new-tpp-email@mail.ua";
        addNewTpp();
        given().obtainTokenFromKeycloak(TPP_LOGIN_NEW, TPP_PASSWORD);
        when().updateSelfTpp(BRANCH, newTppLogin, newTppEmail);
        then().obtainTokenFromKeycloak(newTppLogin, TPP_PASSWORD)
                .readUserFromDb(newTppLogin)
                .verifyUserEntity(user -> {
                    assertThat(user.get("user_id")).isEqualTo(BRANCH);
                    assertThat(user.get("branch")).isEqualTo(BRANCH);
                    assertThat(user.get("email")).isEqualTo(newTppEmail);
                    assertThat(user.get("login")).isEqualTo(newTppLogin);
                });
    }

    @Test
    void testUploadData() {
        addNewTpp();
        given().obtainTokenFromKeycloak(TPP_LOGIN_NEW, "11111");
        when().uploadData("file_upload/users-accounts-balances-payments-upload.yml");
        then().listCustomerLogins()
                .body((List<String> usersLogin) -> assertThat(usersLogin).contains("newtpp","user-one","user-two"));
    }
    private void addNewTpp() {
        given()
                .obtainTokenFromKeycloak(ADMIN_LOGIN, ADMIN_PASSWORD)
                .createNewTppAsAdmin(TPP_LOGIN_NEW, TPP_EMAIL_NEW, BRANCH);
    }
    @Test
    public void testCreateNewTPP() {
        addNewTpp();
        then().readUserFromDb(TPP_LOGIN_NEW)
                .verifyUserEntity(user ->{
                    assertThat(user.get("branch")).isNotNull();
                    assertThat(user.get("email")).isEqualTo(TPP_EMAIL_NEW);
                    assertThat(user.get("login")).isEqualTo(TPP_LOGIN_NEW);
                });
    }
    @Test
    public void testDeleteUserAsTPP(){
        String newUserTppLogin = "exampleintpp";
        String newUserEmail = "examppletpp@gmail.com";
        addNewTpp();
        given().obtainTokenFromKeycloak(TPP_LOGIN_NEW, TPP_PASSWORD).createNewUserAsStaff(newUserTppLogin, newUserEmail, BRANCH);
        then().readUserFromDb(newUserTppLogin)
                .verifyUserEntity(user ->{
                    assertThat(user.get("branch")).isNotNull();
                    assertThat(user.get("email")).isEqualTo(newUserEmail);
                    assertThat(user.get("login")).isEqualTo(newUserTppLogin);
                    assertThat(user.get("user_id")).isNotNull();
                });
        when().deleteUser();
        then().listCustomerLogins().body(login -> assertThat(!login.equals(newUserTppLogin)));
    }

    @Test
    public void testDeleteUser(){
        String newUserLogin = "examplein";
        String newUserEmail = "exampple@gmail.com";
        given().obtainTokenFromKeycloak(ADMIN_LOGIN, ADMIN_PASSWORD).createNewUserAsAdmin(newUserLogin, newUserEmail, BRANCH);
        when().deleteUser();
        then().getAllUsers().body(conc -> assertThat(!conc.equals(newUserLogin)));
    }
    @Test
    public void testCreateNewAdmin() {
        String newAdminLogin = "admin2";
        String newAdminEmail = "newadmin@gmail.com";
        given().obtainTokenFromKeycloak(ADMIN_LOGIN, ADMIN_PASSWORD).createNewUserAsAdmin(newAdminLogin, newAdminEmail, BRANCH);
        then().readUserFromDb(newAdminLogin)
                .verifyUserEntity(user ->{
                    assertThat(user.get("email")).isEqualTo(newAdminEmail);
                    assertThat(user.get("login")).isEqualTo(newAdminLogin);
                });
    }
}
