package com.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chat.common.exception.BusinessException;
import com.chat.mapper.FriendMapper;
import com.chat.mapper.FriendRequestMapper;
import com.chat.mapper.UserMapper;
import com.chat.model.entity.Friend;
import com.chat.model.entity.FriendRequest;
import com.chat.model.entity.User;
import com.chat.model.enums.FriendRequestStatus;
import com.chat.model.vo.FriendRequestVO;
import com.chat.model.vo.FriendVO;
import com.chat.model.vo.SearchUserVO;
import com.chat.model.vo.UserVO;
import com.chat.service.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 好友业务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

	private final FriendMapper friendMapper;
	private final FriendRequestMapper friendRequestMapper;
	private final UserMapper userMapper;

	@Override
	public List<SearchUserVO> searchUsers(Long currentUserId, String keyword) {
		List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
				.ne(User::getId, currentUserId)
				.and(w -> w.eq(User::getPhone, keyword)
						.or().like(User::getNickname, keyword))
				.last("LIMIT 20"));
		return users.stream().map(user -> {
			SearchUserVO vo = SearchUserVO.from(user);
			vo.setRelation(getRelation(currentUserId, user.getId()));
			return vo;
		}).toList();
	}

	@Override
	public void sendRequest(Long fromUserId, Long toUserId, String message) {
		if (fromUserId.equals(toUserId)) {
			throw new BusinessException("不能添加自己为好友");
		}
		if (userMapper.selectById(toUserId) == null) {
			throw new BusinessException("用户不存在");
		}
		if (isFriend(fromUserId, toUserId)) {
			throw new BusinessException("你们已经是好友");
		}
		long pending = friendRequestMapper.selectCount(new LambdaQueryWrapper<FriendRequest>()
				.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode())
				.and(w -> w.and(x -> x.eq(FriendRequest::getFromUserId, fromUserId)
								.eq(FriendRequest::getToUserId, toUserId))
						.or(y -> y.eq(FriendRequest::getFromUserId, toUserId)
								.eq(FriendRequest::getToUserId, fromUserId))));
		if (pending > 0) {
			throw new BusinessException("已存在待处理的好友申请");
		}
		FriendRequest request = new FriendRequest();
		request.setFromUserId(fromUserId);
		request.setToUserId(toUserId);
		request.setMessage(message == null ? "" : message);
		request.setStatus(FriendRequestStatus.PENDING.getCode());
		friendRequestMapper.insert(request);
	}

	@Transactional
	@Override
	public void acceptRequest(Long userId, Long requestId) {
		FriendRequest request = getPendingRequest(requestId);
		if (!request.getToUserId().equals(userId)) {
			throw new BusinessException("无权处理该申请");
		}
		request.setStatus(FriendRequestStatus.ACCEPTED.getCode());
		request.setHandledAt(LocalDateTime.now());
		friendRequestMapper.updateById(request);
		createFriend(request.getFromUserId(), request.getToUserId());
	}

	@Override
	public void rejectRequest(Long userId, Long requestId) {
		FriendRequest request = getPendingRequest(requestId);
		if (!request.getToUserId().equals(userId)) {
			throw new BusinessException("无权处理该申请");
		}
		request.setStatus(FriendRequestStatus.REJECTED.getCode());
		request.setHandledAt(LocalDateTime.now());
		friendRequestMapper.updateById(request);
	}

	@Override
	public List<FriendRequestVO> listReceived(Long userId) {
		List<FriendRequest> requests = friendRequestMapper.selectList(
				new LambdaQueryWrapper<FriendRequest>()
						.eq(FriendRequest::getToUserId, userId)
						.orderByDesc(FriendRequest::getId));
		return toVOList(requests, true);
	}

	@Override
	public List<FriendRequestVO> listSent(Long userId) {
		List<FriendRequest> requests = friendRequestMapper.selectList(
				new LambdaQueryWrapper<FriendRequest>()
						.eq(FriendRequest::getFromUserId, userId)
						.orderByDesc(FriendRequest::getId));
		return toVOList(requests, false);
	}

	@Override
	public List<FriendVO> listFriends(Long userId) {
		List<Friend> friends = friendMapper.selectList(new LambdaQueryWrapper<Friend>()
				.and(w -> w.eq(Friend::getUserId, userId).or().eq(Friend::getFriendId, userId))
				.orderByDesc(Friend::getId));
		if (friends.isEmpty()) {
			return new ArrayList<>();
		}
		List<Long> friendIds = friends.stream()
				.map(f -> f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId())
				.toList();
		Map<Long, User> userMap = userMapper.selectBatchIds(friendIds).stream()
				.collect(Collectors.toMap(User::getId, Function.identity()));
		return friends.stream().map(friend -> {
			Long friendId = friend.getUserId().equals(userId) ? friend.getFriendId() : friend.getUserId();
			FriendVO vo = new FriendVO();
			vo.setFriendId(friendId);
			vo.setRemark(friend.getRemark());
			vo.setUser(UserVO.from(userMap.get(friendId)));
			return vo;
		}).toList();
	}

	@Override
	public void deleteFriend(Long userId, Long friendId) {
		Friend friend = friendMapper.selectOne(new LambdaQueryWrapper<Friend>()
				.and(w -> w.and(x -> x.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
						.or(y -> y.eq(Friend::getUserId, friendId).eq(Friend::getFriendId, userId))));
		if (friend == null) {
			throw new BusinessException("你们还不是好友");
		}
		friendMapper.deleteById(friend.getId());
	}

	@Override
	public int getRelation(Long currentUserId, Long otherUserId) {
		if (currentUserId.equals(otherUserId)) {
			return RELATION_NONE;
		}
		if (isFriend(currentUserId, otherUserId)) {
			return RELATION_FRIEND;
		}
		Long outgoing = friendRequestMapper.selectCount(new LambdaQueryWrapper<FriendRequest>()
				.eq(FriendRequest::getFromUserId, currentUserId)
				.eq(FriendRequest::getToUserId, otherUserId)
				.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode()));
		if (outgoing > 0) {
			return RELATION_OUTGOING;
		}
		Long incoming = friendRequestMapper.selectCount(new LambdaQueryWrapper<FriendRequest>()
				.eq(FriendRequest::getFromUserId, otherUserId)
				.eq(FriendRequest::getToUserId, currentUserId)
				.eq(FriendRequest::getStatus, FriendRequestStatus.PENDING.getCode()));
		if (incoming > 0) {
			return RELATION_INCOMING;
		}
		return RELATION_NONE;
	}

	private boolean isFriend(Long a, Long b) {
		long small = Math.min(a, b);
		long large = Math.max(a, b);
		return friendMapper.selectCount(new LambdaQueryWrapper<Friend>()
				.eq(Friend::getUserId, small)
				.eq(Friend::getFriendId, large)) > 0;
	}

	private FriendRequest getPendingRequest(Long requestId) {
		FriendRequest request = friendRequestMapper.selectById(requestId);
		if (request == null || request.getStatus() == null
				|| request.getStatus() != FriendRequestStatus.PENDING.getCode()) {
			throw new BusinessException("申请不存在或已处理");
		}
		return request;
	}

	private void createFriend(Long a, Long b) {
		long small = Math.min(a, b);
		long large = Math.max(a, b);
		try {
			Friend friend = new Friend();
			friend.setUserId(small);
			friend.setFriendId(large);
			friendMapper.insert(friend);
		} catch (DuplicateKeyException e) {
			log.warn("好友关系已存在：{} - {}", small, large);
		}
	}

	private List<FriendRequestVO> toVOList(List<FriendRequest> requests, boolean received) {
		if (requests.isEmpty()) {
			return new ArrayList<>();
		}
		List<Long> otherIds = requests.stream()
				.map(r -> received ? r.getFromUserId() : r.getToUserId())
				.distinct()
				.toList();
		Map<Long, User> userMap = userMapper.selectBatchIds(otherIds).stream()
				.collect(Collectors.toMap(User::getId, Function.identity()));
		return requests.stream().map(request -> {
			FriendRequestVO vo = FriendRequestVO.from(request);
			if (received) {
				vo.setFromUser(UserVO.from(userMap.get(request.getFromUserId())));
			} else {
				vo.setToUser(UserVO.from(userMap.get(request.getToUserId())));
			}
			return vo;
		}).toList();
	}
}
