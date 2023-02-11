package com.sparta.myselectshop.naver.service;


import com.sparta.myselectshop.naver.dto.ItemDto;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NaverApiService {

    public List<ItemDto> searchItems(String query) {
        RestTemplate rest = new RestTemplate(); //Rest 템플릿을 열고 Header에 아까 id와 Secret 값을 추가한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Naver-Client-Id", "mTr4Momkq5bHExap7ogD");
        headers.add("X-Naver-Client-Secret", "2pZuTcwpzg");
        String body = "";

        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        ResponseEntity<String> responseEntity = rest.exchange("https://openapi.naver.com/v1/search/shop.json?display=15&query=" + query , HttpMethod.GET, requestEntity, String.class);
        // 지정하는 검색어를 쿼리 뒤에다 붙여서 검색을 진행함. display=15 는 총 15개를 보여줄거다. 해당 코드로 네이버 서버로 요청이 가서 responseEntity로 값을 받아온다.
        // 자세한 방법은 Naver Developer 내 document를 참고하면 된다.
        HttpStatus httpStatus = responseEntity.getStatusCode();
        int status = httpStatus.value();
        log.info("NAVER API Status Code : " + status);

        String response = responseEntity.getBody();

        return fromJSONtoItems(response); //fromJSONtoItems라는 메서드로 json으로 받아온 값을 Dto로 변환했다.
    }

    public List<ItemDto> fromJSONtoItems(String response) {

        JSONObject rjson = new JSONObject(response); //가져온 JSON 값을 JSONObject 변수에 넣어준다
        JSONArray items  = rjson.getJSONArray("items"); //그 중에 "items" 인 항목만 뽑아와서 items에 넣어준다.
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (int i=0; i<items.length(); i++) {
            JSONObject itemJson = items.getJSONObject(i);
            ItemDto itemDto = new ItemDto(itemJson);
            itemDtoList.add(itemDto);
        }

        return itemDtoList;
    }
}
