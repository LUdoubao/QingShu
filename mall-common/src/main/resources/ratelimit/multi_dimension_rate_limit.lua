-- 原子化多维度限流脚本
-- 基于令牌桶算法实现，支持多维度组合限流
-- 只有所有维度都满足条件时才扣减令牌，确保原子性

-- 参数说明：
-- KEYS[1..N]: 限流维度键列表
-- ARGV[1]: 当前时间戳（毫秒）
-- ARGV[2]: 申请令牌数
-- ARGV[3]: 时间窗口（毫秒）
-- ARGV[4]: 最大令牌数（窗口内允许的总数）
-- ARGV[5]: 请求唯一标识

local now = tonumber(ARGV[1])
local permits = tonumber(ARGV[2])
local interval = tonumber(ARGV[3])
local maxTokens = tonumber(ARGV[4])

local computedTokens = {}

for i = 1, #KEYS do
	local key = KEYS[i]
	local data = redis.call('HMGET', key, 'tokens', 'ts')
	local tokens = tonumber(data[1])
	local lastTs = tonumber(data[2])

	if tokens == nil or lastTs == nil then
		tokens = maxTokens
		lastTs = now
	end

	local delta = now - lastTs
	if delta < 0 then
		delta = 0
	end

	local refill = (delta / interval) * maxTokens
	local newTokens = tokens + refill
	if newTokens > maxTokens then
		newTokens = maxTokens
	end

	if newTokens < permits then
		return 0
	end

	computedTokens[i] = newTokens
end

for i = 1, #KEYS do
	local key = KEYS[i]
	local remaining = computedTokens[i] - permits
	redis.call('HSET', key, 'tokens', remaining, 'ts', now)
	redis.call('PEXPIRE', key, interval * 2)
end

return 1
