import React, { useState, useEffect } from 'react';
import './DealChat.css';

const DealChat = ({ dealId, prompt }) => {
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!dealId || !prompt) return;

    // Add user message
    setMessages([{ role: 'user', content: prompt }]);
    setIsLoading(true);
    setError(null);

    let buffer = '';

    fetch(`http://localhost:9090/api/deals/${dealId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ prompt }),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        function readStream() {
          reader.read().then(({ done, value }) => {
            if (done) {
              setIsLoading(false);
              return;
            }

            const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;
            
            // Split by newlines and process complete lines
            const lines = buffer.split('\n');
            // Keep the last incomplete line in the buffer
            buffer = lines.pop() || '';

            lines.forEach((line) => {
              line = line.trim();
              if (line.startsWith('data:')) {
                const dataStr = line.substring(5).trim();
                if (dataStr) {
                  try {
                    const data = JSON.parse(dataStr);
                    handleSSEEvent(data);
                  } catch (e) {
                    console.error('Error parsing SSE data:', e, 'Data:', dataStr);
                  }
                }
              }
            });

            readStream();
          }).catch((err) => {
            console.error('Stream reading error:', err);
            setError(err.message);
            setIsLoading(false);
          });
        }

        readStream();
      })
      .catch((error) => {
        console.error('Error connecting to SSE:', error);
        setError(error.message);
        setIsLoading(false);
      });

    return () => {
      // Cleanup if needed
    };
  }, [dealId, prompt]);

  const handleSSEEvent = (data) => {
    const { author, content, actions, finishReason } = data;

    // Handle function calls (agent invocations)
    if (content?.parts) {
      content.parts.forEach((part) => {
        if (part.functionCall) {
          const agentName = part.functionCall.name;
          setMessages((prev) => [
            ...prev,
            {
              role: 'agent_call',
              agentName: agentName,
              content: `Calling ${agentName}...`,
            },
          ]);
        }
      });
    }

    // Handle function responses (agent replies)
    if (content?.parts) {
      content.parts.forEach((part) => {
        if (part.functionResponse) {
          const agentName = part.functionResponse.name;
          const response = part.functionResponse.response?.result;

          setMessages((prev) => {
            const newMessages = [...prev];
            const lastAgentCallIndex = newMessages.findLastIndex(
              (msg) => msg.role === 'agent_call' && msg.agentName === agentName
            );

            if (lastAgentCallIndex !== -1) {
              newMessages[lastAgentCallIndex] = {
                role: 'agent',
                agentName: agentName,
                content: response,
              };
            } else {
              newMessages.push({
                role: 'agent',
                agentName: agentName,
                content: response,
              });
            }
            return newMessages;
          });
        }
      });
    }

    // Handle stateDelta (alternative way to get agent responses)
    if (actions?.stateDelta) {
      const stateDelta = actions.stateDelta;

      Object.entries(stateDelta).forEach(([key, value]) => {
        const agentName = key === 'deal_info' ? 'deal_agent' : 'deal_completeness_agent';
        
        setMessages((prev) => {
          const newMessages = [...prev];
          const existingIndex = newMessages.findIndex(
            (msg) => msg.role === 'agent' && msg.agentName === agentName
          );

          if (existingIndex !== -1) {
            newMessages[existingIndex] = {
              role: 'agent',
              agentName: agentName,
              content: value,
            };
          } else {
            newMessages.push({
              role: 'agent',
              agentName: agentName,
              content: value,
            });
          }
          return newMessages;
        });
      });
    }

    // Handle final response from root agent
    if (finishReason === 'STOP' && author === 'conflict_officer_assistant') {
      setIsLoading(false);
    }
  };

  const getAgentDisplayName = (agentName) => {
    switch (agentName) {
      case 'deal_agent':
        return 'Deal Agent';
      case 'deal_completeness_agent':
        return 'Deal Completeness Agent';
      case 'conflict_officer_assistant':
        return 'Conflict Officer Assistant';
      default:
        return agentName;
    }
  };

  return (
    <div className="deal-chat">
      {error && (
        <div className="error-message">
          Error: {error}
        </div>
      )}
      <div className="messages-container">
        {messages.map((message, index) => (
          <div key={index} className={`message ${message.role}`}>
            {message.role === 'user' && (
              <div className="user-message">
                <div className="message-label">User</div>
                <div className="message-content">{message.content}</div>
              </div>
            )}
            {message.role === 'agent' && (
              <div className="agent-message">
                <div className="message-label">
                  {getAgentDisplayName(message.agentName)}
                </div>
                <div className="message-content">
                  <pre>{message.content}</pre>
                </div>
              </div>
            )}
            {message.role === 'agent_call' && (
              <div className="agent-call-message">
                <div className="message-label">
                  {getAgentDisplayName(message.agentName)}
                </div>
                <div className="message-content loading">
                  {message.content}
                </div>
              </div>
            )}
          </div>
        ))}
        {isLoading && (
          <div className="loading-indicator">
            <div className="spinner"></div>
            <span>Processing...</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default DealChat;
