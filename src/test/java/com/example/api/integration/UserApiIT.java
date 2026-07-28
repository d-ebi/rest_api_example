package com.example.api.integration;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;

@Epic("ユーザーAPI結合テスト")
@Feature("実サーバーを介したユーザーAPI")
@Tag("integration")
@DisplayName("ユーザーAPIのREST Assured結合テスト")
class UserApiIT {

    private static final String USERS_PATH = "/api/v1/users";

    @BeforeAll
    static void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = Integer.getInteger("integration.server.port", 18080);
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("一覧取得でページ情報と初期ユーザーを返す")
    void listUsersReturnsPageAndUsers() {
        given().accept(ContentType.JSON).queryParam("limit", 2).queryParam("offset", 0).when()
                .get(USERS_PATH).then().statusCode(200).contentType(ContentType.JSON)
                .body("count", greaterThanOrEqualTo(3)).body("page.limit", equalTo(2))
                .body("page.offset", equalTo(0)).body("page.total", greaterThanOrEqualTo(3))
                .body("page.hasNext", equalTo(true)).body("users", hasSize(2));
    }

    @Test
    @DisplayName("作成・取得・更新・検索・削除を一連のHTTPリクエストで実行できる")
    void userCrudLifecycleWorksThroughHttp() {
        String uniqueSuffix = UUID.randomUUID().toString();
        String createdName = "REST Assured User " + uniqueSuffix;
        String updatedName = "REST Assured Updated " + uniqueSuffix;

        Map<String, Object> createRequest = Map.of("name", createdName, "age", 30, "birthday",
                "1994/04/01", "height", 170.5, "zipCode", "123-4567", "careerHistories",
                List.of(Map.of("title", "Software Engineer", "period",
                        Map.of("from", "2018/04/01", "to", "2021/03/31"))));

        Response createResponse = given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(createRequest).when().post(USERS_PATH);

        createResponse.then().statusCode(201).header("Location",
                matchesPattern("/api/v1/users/\\d+"));

        long userId = Long.parseLong(createResponse.header("Location")
                .substring(createResponse.header("Location").lastIndexOf('/') + 1));
        boolean deleted = false;

        try {
            given().accept(ContentType.JSON).when().get(USERS_PATH + "/{userId}", userId).then()
                    .statusCode(200).contentType(ContentType.JSON).body("id", equalTo((int) userId))
                    .body("name", equalTo(createdName)).body("age", equalTo(30))
                    .body("birthday", equalTo("1994/04/01")).body("height", equalTo(170.5f))
                    .body("zipCode", equalTo("123-4567")).body("careerHistories", hasSize(1))
                    .body("careerHistories[0].title", equalTo("Software Engineer"))
                    .body("careerHistories[0].period.from", equalTo("2018/04/01"))
                    .body("careerHistories[0].period.to", equalTo("2021/03/31"));

            given().contentType(ContentType.JSON).accept(ContentType.JSON)
                    .body(Map.of("name", updatedName, "age", 31, "zipCode", "987-6543")).when()
                    .put(USERS_PATH + "/{userId}", userId).then().statusCode(204);

            given().accept(ContentType.JSON).queryParam("name", updatedName).queryParam("limit", 10)
                    .queryParam("offset", 0).when().get(USERS_PATH).then().statusCode(200)
                    .body("count", equalTo(1)).body("users", hasSize(1))
                    .body("users[0].id", equalTo((int) userId))
                    .body("users[0].name", equalTo(updatedName)).body("users[0].age", equalTo(31))
                    .body("users[0].zipCode", equalTo("987-6543"));

            given().accept(ContentType.JSON).when().delete(USERS_PATH + "/{userId}", userId).then()
                    .statusCode(204);
            deleted = true;

            given().accept(ContentType.JSON).when().get(USERS_PATH + "/{userId}", userId).then()
                    .statusCode(404).contentType(ContentType.JSON)
                    .body("code", equalTo("NOT_FOUND")).body("traceId", matchesPattern(
                            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
        } finally {
            if (!deleted) {
                given().when().delete(USERS_PATH + "/{userId}", userId);
            }
        }
    }

    @Test
    @DisplayName("必須項目がない作成リクエストは共通エラー形式で400を返す")
    void createUserWithoutRequiredFieldsReturnsBadRequest() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
                .body(Map.of("name", "必須項目不足")).when().post(USERS_PATH).then().statusCode(400)
                .contentType(ContentType.JSON).body("code", equalTo("BAD_REQUEST"))
                .body("traceId",
                        matchesPattern(
                                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
                .body("errors.field", containsInAnyOrder("age", "birthday"));
    }

    @Test
    @DisplayName("ActuatorのヘルスチェックがUPを返す")
    void healthEndpointReturnsUp() {
        given().accept(ContentType.JSON).when().get("/actuator/health").then().statusCode(200)
                .body("status", equalTo("UP"));
    }
}
