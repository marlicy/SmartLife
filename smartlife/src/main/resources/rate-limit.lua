local key = KEYS[1]
local now = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local maxRequests = tonumber(ARGV[3])
local member = ARGV[4]

redis.call('zremrangebyscore', key, 0, now - window * 1000)
local current = redis.call('zcard', key)
if current >= maxRequests then
    return 0
end
redis.call('zadd', key, now, member)
redis.call('expire', key, window)
return 1
