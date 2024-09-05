package com.axonivy.utils.aiassistant.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.portal.components.dto.AiResultDTO;
import com.axonivy.portal.components.enums.AIState;
import com.axonivy.portal.components.persistence.converter.BusinessEntityConverter;
import com.axonivy.utils.aiassistant.demo.dto.MeetingRoom;

public class MeetingRoomService extends BusinessDataService<MeetingRoom> {

  @Override
  public Class<MeetingRoom> getType() {
    return MeetingRoom.class;
  }

  public void createData() {
    deleteAll();
    List<MeetingRoom> data = BusinessEntityConverter
        .jsonValueToEntities(MeetingRoom.DATA, MeetingRoom.class);
    for (MeetingRoom room : data) {
      save(room);
    }
  }

  public List<MeetingRoom> findByCriteria(String name, String floor,
      String isOnlyAvaliable) {
    List<MeetingRoom> allRooms = findAll();

    if (BooleanUtils.toBoolean(isOnlyAvaliable)) {
      allRooms = allRooms.stream()
          .filter(room -> BooleanUtils.isNotFalse(room.isAvailable()))
          .collect(Collectors.toList());
    }

    if (StringUtils.isNotBlank(name)) {
      String[] nameParts = name.split(" ");
      allRooms = allRooms.stream()
          .filter(
              room -> StringUtils.containsAny(room.getRoomName(), nameParts))
          .collect(Collectors.toList());
    }

    Integer floorNumber = NumberUtils.toInt(floor, -1);
    if (floorNumber > 0) {
      allRooms = allRooms.stream()
          .filter(room -> room.getFloor() == floorNumber)
          .collect(Collectors.toList());
    }

    return allRooms;
  }

  public AiResultDTO findByAI(String name, String floor,
      String isOnlyAvaliable) {
    AiResultDTO result = new AiResultDTO();
    List<MeetingRoom> data = findByCriteria(name, floor, isOnlyAvaliable);
    if (CollectionUtils.isEmpty(data)) {
      result.setResult("No avaiable room.");
      result.setResultForAI("No avaiable room.");
      result.setState(AIState.ERROR);
      return result;
    }

    String dataJson = BusinessEntityConverter.entityToJsonValue(data);
    result.setResult(dataJson);
    result.setResultForAI(dataJson);
    result.setState(AIState.DONE);
    return result;
  }

  public MeetingRoom findByMeetingRoomId(String id) {
    return findAll().stream().filter(room -> room.getRoomId().contentEquals(id))
        .findFirst().orElse(null);
  }

  public void deleteAll() {
    List<MeetingRoom> found = findAll();
    if (CollectionUtils.isNotEmpty(found)) {
      for (var room : found) {
        repo().delete(room);
      }
    }
  }
}