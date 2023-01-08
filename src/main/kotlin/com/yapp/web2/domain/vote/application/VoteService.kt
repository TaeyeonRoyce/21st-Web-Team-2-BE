package com.yapp.web2.domain.vote.application

import com.yapp.web2.domain.vote.model.Vote
import com.yapp.web2.domain.vote.repository.VoteQuerydslRepository
import com.yapp.web2.web.api.error.BusinessException
import com.yapp.web2.web.api.error.ErrorCode
import com.yapp.web2.web.dto.vote.response.VoteDetailResponse
import com.yapp.web2.web.dto.vote.response.VotePreviewResponse
import com.yapp.web2.web.dto.voteoption.response.VoteOptionPreviewResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Transactional(readOnly = true)
@Service
class VoteService(
    private val voteQuerydslRepository: VoteQuerydslRepository,
) {
    fun getPopularVotes(): List<VotePreviewResponse> {
        val popularVotes = voteQuerydslRepository.findPopularVotes()
        return popularVotes.map { votePreviewVo ->
            VotePreviewResponse.of(
                votePreviewVo.vote,
                votePreviewVo.commentCount.toInt(),
                votePreviewVo.voteAmount.toInt(),
                getVoteOptionPreviewResponses(votePreviewVo.vote),
            )
        }
    }

    fun getLatestVotesSlice(lastVoteId: Long?): Slice<VotePreviewResponse> {
        val latestVoteSliceVo = voteQuerydslRepository.findLatestVotes(lastVoteId)

        return SliceImpl(
            latestVoteSliceVo.votes.map { votePreviewVo ->
                VotePreviewResponse.of(
                    votePreviewVo.vote,
                    votePreviewVo.commentCount.toInt(),
                    votePreviewVo.voteAmount.toInt(),
                    getVoteOptionPreviewResponses(votePreviewVo.vote),
                )
            },
            Pageable.unpaged(),
            latestVoteSliceVo.hasNext,
        )
    }

    private fun getVoteOptionPreviewResponses(vote: Vote): List<VoteOptionPreviewResponse> {
        return vote.voteOptions.map { voteOption ->
            VoteOptionPreviewResponse.of(
                voteOption,
                //TODO 로그인 한 회원에 대한 투표 여부 응답
            )
        }
    }

    fun getVoteDetail(voteId: Long): VoteDetailResponse {
        try {
            return voteQuerydslRepository.findVoteById(voteId)?.let { voteVo ->
                VoteDetailResponse.of(
                    voteVo.vote,
                    voteVo.voteAmount.toInt(),
                    voteVo.commentCount.toInt(),
                    false, //TODO 좋아요 여부
                    voteVo.likedAmount.toInt(),
                    getVoteOptionPreviewResponses(voteVo.vote),
                )
            } ?: throw BusinessException(ErrorCode.NOT_FOUND_DATA)
        } catch (exception: NoSuchElementException) {
            throw BusinessException(ErrorCode.NOT_FOUND_DATA)
        }
    }
}
