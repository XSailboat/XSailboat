package team.sailboat.base;

/**
 * 
 * Hadoop在zookeeper中注册的高可用性信息解析
 *
 * @author yyl
 * @since 2024年12月19日
 */
public final class HAZKInfoProtos
{
	private HAZKInfoProtos()
	{
	}

	public static void registerAllExtensions(
			com.google.protobuf.ExtensionRegistry registry)
	{
	}

	public interface ActiveNodeInfoOrBuilder
			extends com.google.protobuf.MessageOrBuilder
	{

		// required string nameserviceId = 1;
		/**
		 * <code>required string nameserviceId = 1;</code>
		 */
		boolean hasNameserviceId();

		/**
		 * <code>required string nameserviceId = 1;</code>
		 */
		java.lang.String getNameserviceId();

		/**
		 * <code>required string nameserviceId = 1;</code>
		 */
		com.google.protobuf.ByteString
				getNameserviceIdBytes();

		// required string namenodeId = 2;
		/**
		 * <code>required string namenodeId = 2;</code>
		 */
		boolean hasNamenodeId();

		/**
		 * <code>required string namenodeId = 2;</code>
		 */
		java.lang.String getNamenodeId();

		/**
		 * <code>required string namenodeId = 2;</code>
		 */
		com.google.protobuf.ByteString
				getNamenodeIdBytes();

		// required string hostname = 3;
		/**
		 * <code>required string hostname = 3;</code>
		 */
		boolean hasHostname();

		/**
		 * <code>required string hostname = 3;</code>
		 */
		java.lang.String getHostname();

		/**
		 * <code>required string hostname = 3;</code>
		 */
		com.google.protobuf.ByteString
				getHostnameBytes();

		// required int32 port = 4;
		/**
		 * <code>required int32 port = 4;</code>
		 */
		boolean hasPort();

		/**
		 * <code>required int32 port = 4;</code>
		 */
		int getPort();

		// required int32 zkfcPort = 5;
		/**
		 * <code>required int32 zkfcPort = 5;</code>
		 */
		boolean hasZkfcPort();

		/**
		 * <code>required int32 zkfcPort = 5;</code>
		 */
		int getZkfcPort();
	}

	/**
	 * Protobuf type {@code hadoop.hdfs.ActiveNodeInfo}
	 */
	public static final class ActiveNodeInfo extends
			com.google.protobuf.GeneratedMessage
			implements ActiveNodeInfoOrBuilder
	{
		// Use ActiveNodeInfo.newBuilder() to construct.
		private ActiveNodeInfo(com.google.protobuf.GeneratedMessage.Builder<?> builder)
		{
			super(builder);
			this.unknownFields = builder.getUnknownFields();
		}

		private ActiveNodeInfo(boolean noInit)
		{
			this.unknownFields = com.google.protobuf.UnknownFieldSet.getDefaultInstance();
		}

		private static final ActiveNodeInfo defaultInstance;

		public static ActiveNodeInfo getDefaultInstance()
		{
			return defaultInstance;
		}

		public ActiveNodeInfo getDefaultInstanceForType()
		{
			return defaultInstance;
		}

		private final com.google.protobuf.UnknownFieldSet unknownFields;

		@java.lang.Override
		public final com.google.protobuf.UnknownFieldSet
				getUnknownFields()
		{
			return this.unknownFields;
		}

		private ActiveNodeInfo(
				com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException
		{
			initFields();
			int mutable_bitField0_ = 0;
			com.google.protobuf.UnknownFieldSet.Builder unknownFields = com.google.protobuf.UnknownFieldSet.newBuilder();
			try
			{
				boolean done = false;
				while (!done)
				{
					int tag = input.readTag();
					switch (tag)
					{
					case 0:
						done = true;
						break;
					default:
						{
							if (!parseUnknownField(input,
									unknownFields,
									extensionRegistry,
									tag))
							{
								done = true;
							}
							break;
						}
					case 10:
						{
							bitField0_ |= 0x00000001;
							nameserviceId_ = input.readBytes();
							break;
						}
					case 18:
						{
							bitField0_ |= 0x00000002;
							namenodeId_ = input.readBytes();
							break;
						}
					case 26:
						{
							bitField0_ |= 0x00000004;
							hostname_ = input.readBytes();
							break;
						}
					case 32:
						{
							bitField0_ |= 0x00000008;
							port_ = input.readInt32();
							break;
						}
					case 40:
						{
							bitField0_ |= 0x00000010;
							zkfcPort_ = input.readInt32();
							break;
						}
					}
				}
			}
			catch (com.google.protobuf.InvalidProtocolBufferException e)
			{
				throw e.setUnfinishedMessage(this);
			}
			catch (java.io.IOException e)
			{
				throw new com.google.protobuf.InvalidProtocolBufferException(
						e.getMessage()).setUnfinishedMessage(this);
			}
			finally
			{
				this.unknownFields = unknownFields.build();
				makeExtensionsImmutable();
			}
		}

		public static final com.google.protobuf.Descriptors.Descriptor
				getDescriptor()
		{
			return HAZKInfoProtos.internal_static_hadoop_hdfs_ActiveNodeInfo_descriptor;
		}

		protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
				internalGetFieldAccessorTable()
		{
			return HAZKInfoProtos.internal_static_hadoop_hdfs_ActiveNodeInfo_fieldAccessorTable
																								.ensureFieldAccessorsInitialized(
																										HAZKInfoProtos.ActiveNodeInfo.class,
																										HAZKInfoProtos.ActiveNodeInfo.Builder.class);
		}

		public static com.google.protobuf.Parser<ActiveNodeInfo> PARSER = new com.google.protobuf.AbstractParser<ActiveNodeInfo>()
		{
			public ActiveNodeInfo parsePartialFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws com.google.protobuf.InvalidProtocolBufferException
			{
				return new ActiveNodeInfo(input, extensionRegistry);
			}
		};

		@java.lang.Override
		public com.google.protobuf.Parser<ActiveNodeInfo> getParserForType()
		{
			return PARSER;
		}

		private int bitField0_;
		// required string nameserviceId = 1;
		public static final int NAMESERVICEID_FIELD_NUMBER = 1;
		private java.lang.Object nameserviceId_;

		/**
		 * <code>required string nameserviceId = 1;</code>
		 */
		public boolean hasNameserviceId()
		{
			return ((bitField0_ & 0x00000001) == 0x00000001);
		}

		/**
		 * <code>required string nameserviceId = 1;</code>
		 */
		public java.lang.String getNameserviceId()
		{
			java.lang.Object ref = nameserviceId_;
			if (ref instanceof java.lang.String)
			{
				return (java.lang.String) ref;
			}
			else
			{
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8())
				{
					nameserviceId_ = s;
				}
				return s;
			}
		}

		/**
		 * <code>required string nameserviceId = 1;</code>
		 */
		public com.google.protobuf.ByteString
				getNameserviceIdBytes()
		{
			java.lang.Object ref = nameserviceId_;
			if (ref instanceof java.lang.String)
			{
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8(
						(java.lang.String) ref);
				nameserviceId_ = b;
				return b;
			}
			else
			{
				return (com.google.protobuf.ByteString) ref;
			}
		}

		// required string namenodeId = 2;
		public static final int NAMENODEID_FIELD_NUMBER = 2;
		private java.lang.Object namenodeId_;

		/**
		 * <code>required string namenodeId = 2;</code>
		 */
		public boolean hasNamenodeId()
		{
			return ((bitField0_ & 0x00000002) == 0x00000002);
		}

		/**
		 * <code>required string namenodeId = 2;</code>
		 */
		public java.lang.String getNamenodeId()
		{
			java.lang.Object ref = namenodeId_;
			if (ref instanceof java.lang.String)
			{
				return (java.lang.String) ref;
			}
			else
			{
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8())
				{
					namenodeId_ = s;
				}
				return s;
			}
		}

		/**
		 * <code>required string namenodeId = 2;</code>
		 */
		public com.google.protobuf.ByteString
				getNamenodeIdBytes()
		{
			java.lang.Object ref = namenodeId_;
			if (ref instanceof java.lang.String)
			{
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8(
						(java.lang.String) ref);
				namenodeId_ = b;
				return b;
			}
			else
			{
				return (com.google.protobuf.ByteString) ref;
			}
		}

		// required string hostname = 3;
		public static final int HOSTNAME_FIELD_NUMBER = 3;
		private java.lang.Object hostname_;

		/**
		 * <code>required string hostname = 3;</code>
		 */
		public boolean hasHostname()
		{
			return ((bitField0_ & 0x00000004) == 0x00000004);
		}

		/**
		 * <code>required string hostname = 3;</code>
		 */
		public java.lang.String getHostname()
		{
			java.lang.Object ref = hostname_;
			if (ref instanceof java.lang.String)
			{
				return (java.lang.String) ref;
			}
			else
			{
				com.google.protobuf.ByteString bs = (com.google.protobuf.ByteString) ref;
				java.lang.String s = bs.toStringUtf8();
				if (bs.isValidUtf8())
				{
					hostname_ = s;
				}
				return s;
			}
		}

		/**
		 * <code>required string hostname = 3;</code>
		 */
		public com.google.protobuf.ByteString
				getHostnameBytes()
		{
			java.lang.Object ref = hostname_;
			if (ref instanceof java.lang.String)
			{
				com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8(
						(java.lang.String) ref);
				hostname_ = b;
				return b;
			}
			else
			{
				return (com.google.protobuf.ByteString) ref;
			}
		}

		// required int32 port = 4;
		public static final int PORT_FIELD_NUMBER = 4;
		private int port_;

		/**
		 * <code>required int32 port = 4;</code>
		 */
		public boolean hasPort()
		{
			return ((bitField0_ & 0x00000008) == 0x00000008);
		}

		/**
		 * <code>required int32 port = 4;</code>
		 */
		public int getPort()
		{
			return port_;
		}

		// required int32 zkfcPort = 5;
		public static final int ZKFCPORT_FIELD_NUMBER = 5;
		private int zkfcPort_;

		/**
		 * <code>required int32 zkfcPort = 5;</code>
		 */
		public boolean hasZkfcPort()
		{
			return ((bitField0_ & 0x00000010) == 0x00000010);
		}

		/**
		 * <code>required int32 zkfcPort = 5;</code>
		 */
		public int getZkfcPort()
		{
			return zkfcPort_;
		}

		private void initFields()
		{
			nameserviceId_ = "";
			namenodeId_ = "";
			hostname_ = "";
			port_ = 0;
			zkfcPort_ = 0;
		}

		private byte memoizedIsInitialized = -1;

		public final boolean isInitialized()
		{
			byte isInitialized = memoizedIsInitialized;
			if (isInitialized != -1)
				return isInitialized == 1;

			if (!hasNameserviceId())
			{
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasNamenodeId())
			{
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasHostname())
			{
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasPort())
			{
				memoizedIsInitialized = 0;
				return false;
			}
			if (!hasZkfcPort())
			{
				memoizedIsInitialized = 0;
				return false;
			}
			memoizedIsInitialized = 1;
			return true;
		}

		public void writeTo(com.google.protobuf.CodedOutputStream output)
				throws java.io.IOException
		{
			getSerializedSize();
			if (((bitField0_ & 0x00000001) == 0x00000001))
			{
				output.writeBytes(1, getNameserviceIdBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002))
			{
				output.writeBytes(2, getNamenodeIdBytes());
			}
			if (((bitField0_ & 0x00000004) == 0x00000004))
			{
				output.writeBytes(3, getHostnameBytes());
			}
			if (((bitField0_ & 0x00000008) == 0x00000008))
			{
				output.writeInt32(4, port_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010))
			{
				output.writeInt32(5, zkfcPort_);
			}
			getUnknownFields().writeTo(output);
		}

		private int memoizedSerializedSize = -1;

		public int getSerializedSize()
		{
			int size = memoizedSerializedSize;
			if (size != -1)
				return size;

			size = 0;
			if (((bitField0_ & 0x00000001) == 0x00000001))
			{
				size += com.google.protobuf.CodedOutputStream
																.computeBytesSize(1, getNameserviceIdBytes());
			}
			if (((bitField0_ & 0x00000002) == 0x00000002))
			{
				size += com.google.protobuf.CodedOutputStream
																.computeBytesSize(2, getNamenodeIdBytes());
			}
			if (((bitField0_ & 0x00000004) == 0x00000004))
			{
				size += com.google.protobuf.CodedOutputStream
																.computeBytesSize(3, getHostnameBytes());
			}
			if (((bitField0_ & 0x00000008) == 0x00000008))
			{
				size += com.google.protobuf.CodedOutputStream
																.computeInt32Size(4, port_);
			}
			if (((bitField0_ & 0x00000010) == 0x00000010))
			{
				size += com.google.protobuf.CodedOutputStream
																.computeInt32Size(5, zkfcPort_);
			}
			size += getUnknownFields().getSerializedSize();
			memoizedSerializedSize = size;
			return size;
		}

		private static final long serialVersionUID = 0L;

		@java.lang.Override
		protected java.lang.Object writeReplace()
				throws java.io.ObjectStreamException
		{
			return super.writeReplace();
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(
				com.google.protobuf.ByteString data)
				throws com.google.protobuf.InvalidProtocolBufferException
		{
			return PARSER.parseFrom(data);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(
				com.google.protobuf.ByteString data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException
		{
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(byte[] data)
				throws com.google.protobuf.InvalidProtocolBufferException
		{
			return PARSER.parseFrom(data);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(
				byte[] data,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws com.google.protobuf.InvalidProtocolBufferException
		{
			return PARSER.parseFrom(data, extensionRegistry);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(java.io.InputStream input)
				throws java.io.IOException
		{
			return PARSER.parseFrom(input);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(
				java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws java.io.IOException
		{
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseDelimitedFrom(java.io.InputStream input)
				throws java.io.IOException
		{
			return PARSER.parseDelimitedFrom(input);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseDelimitedFrom(
				java.io.InputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws java.io.IOException
		{
			return PARSER.parseDelimitedFrom(input, extensionRegistry);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(
				com.google.protobuf.CodedInputStream input)
				throws java.io.IOException
		{
			return PARSER.parseFrom(input);
		}

		public static HAZKInfoProtos.ActiveNodeInfo parseFrom(
				com.google.protobuf.CodedInputStream input,
				com.google.protobuf.ExtensionRegistryLite extensionRegistry)
				throws java.io.IOException
		{
			return PARSER.parseFrom(input, extensionRegistry);
		}

		public static Builder newBuilder()
		{
			return Builder.create();
		}

		public Builder newBuilderForType()
		{
			return newBuilder();
		}

		public static Builder newBuilder(HAZKInfoProtos.ActiveNodeInfo prototype)
		{
			return newBuilder().mergeFrom(prototype);
		}

		public Builder toBuilder()
		{
			return newBuilder(this);
		}

		@java.lang.Override
		protected Builder newBuilderForType(
				com.google.protobuf.GeneratedMessage.BuilderParent parent)
		{
			Builder builder = new Builder(parent);
			return builder;
		}

		/**
		 * Protobuf type {@code hadoop.hdfs.ActiveNodeInfo}
		 */
		public static final class Builder extends
				com.google.protobuf.GeneratedMessage.Builder<Builder>
				implements HAZKInfoProtos.ActiveNodeInfoOrBuilder
		{
			public static final com.google.protobuf.Descriptors.Descriptor
					getDescriptor()
			{
				return HAZKInfoProtos.internal_static_hadoop_hdfs_ActiveNodeInfo_descriptor;
			}

			protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
					internalGetFieldAccessorTable()
			{
				return HAZKInfoProtos.internal_static_hadoop_hdfs_ActiveNodeInfo_fieldAccessorTable
																									.ensureFieldAccessorsInitialized(
																											HAZKInfoProtos.ActiveNodeInfo.class,
																											HAZKInfoProtos.ActiveNodeInfo.Builder.class);
			}

			// Construct using HAZKInfoProtos.ActiveNodeInfo.newBuilder()
			private Builder()
			{
				maybeForceBuilderInitialization();
			}

			private Builder(
					com.google.protobuf.GeneratedMessage.BuilderParent parent)
			{
				super(parent);
				maybeForceBuilderInitialization();
			}

			private void maybeForceBuilderInitialization()
			{
				if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders)
				{
				}
			}

			private static Builder create()
			{
				return new Builder();
			}

			public Builder clear()
			{
				super.clear();
				nameserviceId_ = "";
				bitField0_ = (bitField0_ & ~0x00000001);
				namenodeId_ = "";
				bitField0_ = (bitField0_ & ~0x00000002);
				hostname_ = "";
				bitField0_ = (bitField0_ & ~0x00000004);
				port_ = 0;
				bitField0_ = (bitField0_ & ~0x00000008);
				zkfcPort_ = 0;
				bitField0_ = (bitField0_ & ~0x00000010);
				return this;
			}

			public Builder clone()
			{
				return create().mergeFrom(buildPartial());
			}

			public com.google.protobuf.Descriptors.Descriptor
					getDescriptorForType()
			{
				return HAZKInfoProtos.internal_static_hadoop_hdfs_ActiveNodeInfo_descriptor;
			}

			public HAZKInfoProtos.ActiveNodeInfo getDefaultInstanceForType()
			{
				return HAZKInfoProtos.ActiveNodeInfo.getDefaultInstance();
			}

			public HAZKInfoProtos.ActiveNodeInfo build()
			{
				HAZKInfoProtos.ActiveNodeInfo result = buildPartial();
				if (!result.isInitialized())
				{
					throw newUninitializedMessageException(result);
				}
				return result;
			}

			public HAZKInfoProtos.ActiveNodeInfo buildPartial()
			{
				HAZKInfoProtos.ActiveNodeInfo result = new HAZKInfoProtos.ActiveNodeInfo(this);
				int from_bitField0_ = bitField0_;
				int to_bitField0_ = 0;
				if (((from_bitField0_ & 0x00000001) == 0x00000001))
				{
					to_bitField0_ |= 0x00000001;
				}
				result.nameserviceId_ = nameserviceId_;
				if (((from_bitField0_ & 0x00000002) == 0x00000002))
				{
					to_bitField0_ |= 0x00000002;
				}
				result.namenodeId_ = namenodeId_;
				if (((from_bitField0_ & 0x00000004) == 0x00000004))
				{
					to_bitField0_ |= 0x00000004;
				}
				result.hostname_ = hostname_;
				if (((from_bitField0_ & 0x00000008) == 0x00000008))
				{
					to_bitField0_ |= 0x00000008;
				}
				result.port_ = port_;
				if (((from_bitField0_ & 0x00000010) == 0x00000010))
				{
					to_bitField0_ |= 0x00000010;
				}
				result.zkfcPort_ = zkfcPort_;
				result.bitField0_ = to_bitField0_;
				onBuilt();
				return result;
			}

			public Builder mergeFrom(com.google.protobuf.Message other)
			{
				if (other instanceof HAZKInfoProtos.ActiveNodeInfo)
				{
					return mergeFrom((HAZKInfoProtos.ActiveNodeInfo) other);
				}
				else
				{
					super.mergeFrom(other);
					return this;
				}
			}

			public Builder mergeFrom(HAZKInfoProtos.ActiveNodeInfo other)
			{
				if (other == HAZKInfoProtos.ActiveNodeInfo.getDefaultInstance())
					return this;
				if (other.hasNameserviceId())
				{
					bitField0_ |= 0x00000001;
					nameserviceId_ = other.nameserviceId_;
					onChanged();
				}
				if (other.hasNamenodeId())
				{
					bitField0_ |= 0x00000002;
					namenodeId_ = other.namenodeId_;
					onChanged();
				}
				if (other.hasHostname())
				{
					bitField0_ |= 0x00000004;
					hostname_ = other.hostname_;
					onChanged();
				}
				if (other.hasPort())
				{
					setPort(other.getPort());
				}
				if (other.hasZkfcPort())
				{
					setZkfcPort(other.getZkfcPort());
				}
				this.mergeUnknownFields(other.getUnknownFields());
				return this;
			}

			public final boolean isInitialized()
			{
				if (!hasNameserviceId())
				{

					return false;
				}
				if (!hasNamenodeId())
				{

					return false;
				}
				if (!hasHostname())
				{

					return false;
				}
				if (!hasPort())
				{

					return false;
				}
				if (!hasZkfcPort())
				{

					return false;
				}
				return true;
			}

			public Builder mergeFrom(
					com.google.protobuf.CodedInputStream input,
					com.google.protobuf.ExtensionRegistryLite extensionRegistry)
					throws java.io.IOException
			{
				HAZKInfoProtos.ActiveNodeInfo parsedMessage = null;
				try
				{
					parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
				}
				catch (com.google.protobuf.InvalidProtocolBufferException e)
				{
					parsedMessage = (HAZKInfoProtos.ActiveNodeInfo) e.getUnfinishedMessage();
					throw e;
				}
				finally
				{
					if (parsedMessage != null)
					{
						mergeFrom(parsedMessage);
					}
				}
				return this;
			}

			private int bitField0_;

			// required string nameserviceId = 1;
			private java.lang.Object nameserviceId_ = "";

			/**
			 * <code>required string nameserviceId = 1;</code>
			 */
			public boolean hasNameserviceId()
			{
				return ((bitField0_ & 0x00000001) == 0x00000001);
			}

			/**
			 * <code>required string nameserviceId = 1;</code>
			 */
			public java.lang.String getNameserviceId()
			{
				java.lang.Object ref = nameserviceId_;
				if (!(ref instanceof java.lang.String))
				{
					java.lang.String s = ((com.google.protobuf.ByteString) ref)
																				.toStringUtf8();
					nameserviceId_ = s;
					return s;
				}
				else
				{
					return (java.lang.String) ref;
				}
			}

			/**
			 * <code>required string nameserviceId = 1;</code>
			 */
			public com.google.protobuf.ByteString
					getNameserviceIdBytes()
			{
				java.lang.Object ref = nameserviceId_;
				if (ref instanceof String)
				{
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8(
							(java.lang.String) ref);
					nameserviceId_ = b;
					return b;
				}
				else
				{
					return (com.google.protobuf.ByteString) ref;
				}
			}

			/**
			 * <code>required string nameserviceId = 1;</code>
			 */
			public Builder setNameserviceId(
					java.lang.String value)
			{
				if (value == null)
				{
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				nameserviceId_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>required string nameserviceId = 1;</code>
			 */
			public Builder clearNameserviceId()
			{
				bitField0_ = (bitField0_ & ~0x00000001);
				nameserviceId_ = getDefaultInstance().getNameserviceId();
				onChanged();
				return this;
			}

			/**
			 * <code>required string nameserviceId = 1;</code>
			 */
			public Builder setNameserviceIdBytes(
					com.google.protobuf.ByteString value)
			{
				if (value == null)
				{
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000001;
				nameserviceId_ = value;
				onChanged();
				return this;
			}

			// required string namenodeId = 2;
			private java.lang.Object namenodeId_ = "";

			/**
			 * <code>required string namenodeId = 2;</code>
			 */
			public boolean hasNamenodeId()
			{
				return ((bitField0_ & 0x00000002) == 0x00000002);
			}

			/**
			 * <code>required string namenodeId = 2;</code>
			 */
			public java.lang.String getNamenodeId()
			{
				java.lang.Object ref = namenodeId_;
				if (!(ref instanceof java.lang.String))
				{
					java.lang.String s = ((com.google.protobuf.ByteString) ref)
																				.toStringUtf8();
					namenodeId_ = s;
					return s;
				}
				else
				{
					return (java.lang.String) ref;
				}
			}

			/**
			 * <code>required string namenodeId = 2;</code>
			 */
			public com.google.protobuf.ByteString
					getNamenodeIdBytes()
			{
				java.lang.Object ref = namenodeId_;
				if (ref instanceof String)
				{
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8(
							(java.lang.String) ref);
					namenodeId_ = b;
					return b;
				}
				else
				{
					return (com.google.protobuf.ByteString) ref;
				}
			}

			/**
			 * <code>required string namenodeId = 2;</code>
			 */
			public Builder setNamenodeId(
					java.lang.String value)
			{
				if (value == null)
				{
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				namenodeId_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>required string namenodeId = 2;</code>
			 */
			public Builder clearNamenodeId()
			{
				bitField0_ = (bitField0_ & ~0x00000002);
				namenodeId_ = getDefaultInstance().getNamenodeId();
				onChanged();
				return this;
			}

			/**
			 * <code>required string namenodeId = 2;</code>
			 */
			public Builder setNamenodeIdBytes(
					com.google.protobuf.ByteString value)
			{
				if (value == null)
				{
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000002;
				namenodeId_ = value;
				onChanged();
				return this;
			}

			// required string hostname = 3;
			private java.lang.Object hostname_ = "";

			/**
			 * <code>required string hostname = 3;</code>
			 */
			public boolean hasHostname()
			{
				return ((bitField0_ & 0x00000004) == 0x00000004);
			}

			/**
			 * <code>required string hostname = 3;</code>
			 */
			public java.lang.String getHostname()
			{
				java.lang.Object ref = hostname_;
				if (!(ref instanceof java.lang.String))
				{
					java.lang.String s = ((com.google.protobuf.ByteString) ref)
																				.toStringUtf8();
					hostname_ = s;
					return s;
				}
				else
				{
					return (java.lang.String) ref;
				}
			}

			/**
			 * <code>required string hostname = 3;</code>
			 */
			public com.google.protobuf.ByteString
					getHostnameBytes()
			{
				java.lang.Object ref = hostname_;
				if (ref instanceof String)
				{
					com.google.protobuf.ByteString b = com.google.protobuf.ByteString.copyFromUtf8(
							(java.lang.String) ref);
					hostname_ = b;
					return b;
				}
				else
				{
					return (com.google.protobuf.ByteString) ref;
				}
			}

			/**
			 * <code>required string hostname = 3;</code>
			 */
			public Builder setHostname(
					java.lang.String value)
			{
				if (value == null)
				{
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				hostname_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>required string hostname = 3;</code>
			 */
			public Builder clearHostname()
			{
				bitField0_ = (bitField0_ & ~0x00000004);
				hostname_ = getDefaultInstance().getHostname();
				onChanged();
				return this;
			}

			/**
			 * <code>required string hostname = 3;</code>
			 */
			public Builder setHostnameBytes(
					com.google.protobuf.ByteString value)
			{
				if (value == null)
				{
					throw new NullPointerException();
				}
				bitField0_ |= 0x00000004;
				hostname_ = value;
				onChanged();
				return this;
			}

			// required int32 port = 4;
			private int port_;

			/**
			 * <code>required int32 port = 4;</code>
			 */
			public boolean hasPort()
			{
				return ((bitField0_ & 0x00000008) == 0x00000008);
			}

			/**
			 * <code>required int32 port = 4;</code>
			 */
			public int getPort()
			{
				return port_;
			}

			/**
			 * <code>required int32 port = 4;</code>
			 */
			public Builder setPort(int value)
			{
				bitField0_ |= 0x00000008;
				port_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>required int32 port = 4;</code>
			 */
			public Builder clearPort()
			{
				bitField0_ = (bitField0_ & ~0x00000008);
				port_ = 0;
				onChanged();
				return this;
			}

			// required int32 zkfcPort = 5;
			private int zkfcPort_;

			/**
			 * <code>required int32 zkfcPort = 5;</code>
			 */
			public boolean hasZkfcPort()
			{
				return ((bitField0_ & 0x00000010) == 0x00000010);
			}

			/**
			 * <code>required int32 zkfcPort = 5;</code>
			 */
			public int getZkfcPort()
			{
				return zkfcPort_;
			}

			/**
			 * <code>required int32 zkfcPort = 5;</code>
			 */
			public Builder setZkfcPort(int value)
			{
				bitField0_ |= 0x00000010;
				zkfcPort_ = value;
				onChanged();
				return this;
			}

			/**
			 * <code>required int32 zkfcPort = 5;</code>
			 */
			public Builder clearZkfcPort()
			{
				bitField0_ = (bitField0_ & ~0x00000010);
				zkfcPort_ = 0;
				onChanged();
				return this;
			}

			// @@protoc_insertion_point(builder_scope:hadoop.hdfs.ActiveNodeInfo)
		}

		static
		{
			defaultInstance = new ActiveNodeInfo(true);
			defaultInstance.initFields();
		}

		// @@protoc_insertion_point(class_scope:hadoop.hdfs.ActiveNodeInfo)
	}

	private static com.google.protobuf.Descriptors.Descriptor internal_static_hadoop_hdfs_ActiveNodeInfo_descriptor;
	private static com.google.protobuf.GeneratedMessage.FieldAccessorTable internal_static_hadoop_hdfs_ActiveNodeInfo_fieldAccessorTable;

	public static com.google.protobuf.Descriptors.FileDescriptor
			getDescriptor()
	{
		return descriptor;
	}

	private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
	static
	{
		java.lang.String[] descriptorData = {
				"\n\016HAZKInfo.proto\022\013hadoop.hdfs\"m\n\016ActiveN" +
						"odeInfo\022\025\n\rnameserviceId\030\001 \002(\t\022\022\n\nnameno"
						+
						"deId\030\002 \002(\t\022\020\n\010hostname\030\003 \002(\t\022\014\n\004port\030\004 \002"
						+
						"(\005\022\020\n\010zkfcPort\030\005 \002(\005BA\n/org.apache.hadoo"
						+
						"p.hdfs.server.namenode.ha.protoB\016HAZKInf"
						+
						"oProtos"
		};
		com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner = new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner()
		{
			public com.google.protobuf.ExtensionRegistry assignDescriptors(
					com.google.protobuf.Descriptors.FileDescriptor root)
			{
				descriptor = root;
				internal_static_hadoop_hdfs_ActiveNodeInfo_descriptor = getDescriptor().getMessageTypes().get(0);
				internal_static_hadoop_hdfs_ActiveNodeInfo_fieldAccessorTable = new com.google.protobuf.GeneratedMessage.FieldAccessorTable(
						internal_static_hadoop_hdfs_ActiveNodeInfo_descriptor,
						new java.lang.String[] { "NameserviceId", "NamenodeId", "Hostname", "Port", "ZkfcPort", });
				return null;
			}
		};
		com.google.protobuf.Descriptors.FileDescriptor
														.internalBuildGeneratedFileFrom(descriptorData,
																new com.google.protobuf.Descriptors.FileDescriptor[] {
																},
																assigner);
	}

	// @@protoc_insertion_point(outer_class_scope)
}
